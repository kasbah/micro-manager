///////////////////////////////////////////////////////////////////////////////
// FILE:          HiSeq2x00Camera.cpp
// PROJECT:       Micro-Manager
// SUBSYSTEM:     DeviceAdapters
//-----------------------------------------------------------------------------
// DESCRIPTION:   Illumina HiSeq 2000 and 2500 Hamamatsu camera based on DCAM API
// COPYRIGHT:     Kaspar Emanuel 2019
//
//
// LICENSE:       This library is free software; you can redistribute it and/or
//                modify it under the terms of the GNU Lesser General Public
//                License as published by the Free Software Foundation.
//
//                You should have received a copy of the GNU Lesser General Public
//                License along with the source distribution; if not, write to
//                the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
//                Boston, MA  02111-1307  USA
//
//                This file is distributed in the hope that it will be useful,
//                but WITHOUT ANY WARRANTY; without even the implied warranty
//                of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
//                IN NO EVENT SHALL THE COPYRIGHT OWNER OR
//                CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
//                INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES.
// AUTHOR:        Kaspar Emanuel
//
// CVS:           $Id$
//
#include "HiSeq2x00Camera.h"

#include <string>

using namespace std;

const char* g_DeviceName = "HiSeq2x00Camera";

CHiSeq2x00Camera::CHiSeq2x00Camera() : busy_(false)
{
	img_.Resize(4096, 4000, 2);
}

CHiSeq2x00Camera::~CHiSeq2x00Camera()
{}

void CHiSeq2x00Camera::GetName(char* name) const
{
	CDeviceUtils::CopyLimitedString(name, g_DeviceName);
}

inline const int my_dcamdev_string(DCAMERR& err, HDCAM hdcam, int32 idStr, char* text, int32 textbytes)
{
	DCAMDEV_STRING	param;
	memset(&param, 0, sizeof(param));
	param.size = sizeof(param);
	param.text = text;
	param.textbytes = textbytes;
	param.iString = idStr;

	err = dcamdev_getstring(hdcam, &param);
	return !failed(err);
}



int CHiSeq2x00Camera::Initialize()
{
	int ret = DEVICE_OK;
	DCAMERR err;

	DCAMAPI_INIT apiinit;
	memset(&apiinit, 0, sizeof(apiinit));
	apiinit.size = sizeof(apiinit);
	err = dcamapi_init(&apiinit);

	if (failed(err)) {
		LogMessage("Error initializing dcamapi");
		return DEVICE_ERR;
	}

	DCAMDEV_OPEN devopen;
	memset(&devopen, 0, sizeof(devopen));
	devopen.size = sizeof(devopen);
	devopen.index = 0;
	err = dcamdev_open(&devopen);

	if (failed(err)) {
		LogMessage("Error opening device");
		return DEVICE_ERR;
	}

	hdcam_ = devopen.hdcam;

	{
		char model[256];
		char cameraid[64];
		char bus[64];
		char msg[384] = "Hellooo from HiSeq2x00Camera: ";

		DCAMERR	err;
		my_dcamdev_string(err, hdcam_, DCAM_IDSTR_MODEL, model, sizeof(model));

		if (failed(err))
			LogMessage("Error obtaining model");
		else {
			LogMessage(strcat(msg, model));
		}

	}
	DCAMERR e;
	e = dcamprop_setvalue(hdcam_, DCAM_IDPROP_TRIGGERSOURCE, DCAMPROP_TRIGGERSOURCE__EXTERNAL);
	if (failed(e)) {
		LogMessage("Error setting trigger source");
		char x[64];
		sprintf(x, "DCAMERR: 0x%08X", e);
		LogMessage(x);
		return DEVICE_ERR;
	}

	e = dcamprop_setvalue(hdcam_, DCAM_IDPROP_SENSORMODE_LINEBUNDLEHEIGHT, 4000);
	if (failed(e)) {
		LogMessage("Error setting line bundle height");
		char x[64];
		sprintf(x, "DCAMERR: 0x%08X", e);
		LogMessage(x);
		return DEVICE_ERR;
	}

	img_.Resize(4096, 4000, 2);

	LogMessage("init done");
	return ret;
}

int CHiSeq2x00Camera::SetBinning(int binSize)
{
	ostringstream os;
	os << binSize;
	return SetProperty(MM::g_Keyword_Binning, os.str().c_str());
}
int CHiSeq2x00Camera::Shutdown()
{
	dcamdev_close(hdcam_);
	DCAMERR err = dcamapi_uninit();
	if (failed(err)) {
		return DEVICE_ERR;
	}
	return DEVICE_OK;
}


bool CHiSeq2x00Camera::Busy(){
	return busy_;
}

void dcamcon_show_dcamerr(HDCAM hdcam, DCAMERR errid, const char* apiname, const char* fmt, ...)
{
	char errtext[256];

	DCAMERR err;
	my_dcamdev_string(err, hdcam, errid, errtext, sizeof(errtext));

	printf("FAILED: (DCAMERR)0x%08X %s @ %s", errid, errtext, apiname);

	if (fmt != NULL)
	{
		printf(" : ");

		va_list	arg;
		va_start(arg, fmt);
		vprintf(fmt, arg);
		va_end(arg);
	}

	printf("\n");
}
/**
 @brief	get image information from properties.
 @param	hdcam:		DCAM handle
 @param pixeltype:	DCAM_PIXELTYPE value
 @param width:		image width
 @param rowbytes:	image rowbytes
 @param height:		image height
 */
void get_image_information(HDCAM hdcam, int32& pixeltype, int32& width, int32& rowbytes, int32& height)
{
	DCAMERR err;

	double v;

	// image pixel type(DCAM_PIXELTYPE_MONO16, MONO8, ... )
	err = dcamprop_getvalue(hdcam, DCAM_IDPROP_IMAGE_PIXELTYPE, &v);
	if (failed(err))
	{
		dcamcon_show_dcamerr(hdcam, err, "dcamprop_getvalue()", "IDPROP:IMAGE_PIXELTYPE");
		return;
	}
	else
		pixeltype = (int32)v;

	// image width
	err = dcamprop_getvalue(hdcam, DCAM_IDPROP_IMAGE_WIDTH, &v);
	if (failed(err))
	{
		dcamcon_show_dcamerr(hdcam, err, "dcamprop_getvalue()", "IDPROP:IMAGE_WIDTH");
		return;
	}
	else
		width = (int32)v;

	// image row bytes
	err = dcamprop_getvalue(hdcam, DCAM_IDPROP_IMAGE_ROWBYTES, &v);
	if (failed(err))
	{
		dcamcon_show_dcamerr(hdcam, err, "dcamprop_getvalue()", "IDPROP:IMAGE_ROWBYTES");
		return;
	}
	else
		rowbytes = (int32)v;

	// image height
	err = dcamprop_getvalue(hdcam, DCAM_IDPROP_IMAGE_HEIGHT, &v);
	if (failed(err))
	{
		dcamcon_show_dcamerr(hdcam, err, "dcamprop_getvalue()", "IDPROP:IMAGE_HEIGHT");
		return;
	}
	else
		height = (int32)v;
}


int CHiSeq2x00Camera::SnapImage()
{
	LogMessage("SnapImage");
	busy_ = true;
	// open wait handle
	DCAMWAIT_OPEN	waitopen;
	memset(&waitopen, 0, sizeof(waitopen));
	waitopen.size = sizeof(waitopen);
	waitopen.hdcam = hdcam_;

	DCAMERR err = dcamwait_open(&waitopen);
	
	hwait_ = waitopen.hwait;

	LogMessage("Allocate");
	int32 number_of_buffer = 10;
	err = dcambuf_alloc(hdcam_, number_of_buffer);
	err = dcamcap_start(hdcam_, DCAMCAP_START_SEQUENCE);
	LogMessage("Allocate done");
	// set wait param
	DCAMWAIT_START waitstart;
	memset(&waitstart, 0, sizeof(waitstart));
	waitstart.size = sizeof(waitstart);
	waitstart.eventmask = DCAMWAIT_CAPEVENT_FRAMEREADY;
	waitstart.timeout = 30000;

	//dcamcap_firetrigger(hdcam_, 0);
	LogMessage("wait_start");
	err = dcamwait_start(hwait_, &waitstart);
	if (failed(err)) {
		LogMessage("Error during wait_start");
		char x[64];
		sprintf(x, "DCAMERR: 0x%08X", err);
		LogMessage(x);
		return DEVICE_ERR;
	}
	dcamcap_stop(hdcam_);

	
	LogMessage("SnapImage done");
	return DEVICE_OK;
}

BOOL copy_targetarea(HDCAM hdcam, int32 iFrame, void* buf, int32 rowbytes, int32 ox, int32 oy, int32 cx, int32 cy)
{
	DCAMERR err;

	// prepare frame param
	DCAMBUF_FRAME bufframe;
	memset(&bufframe, 0, sizeof(bufframe));
	bufframe.size = sizeof(bufframe);
	bufframe.iFrame = iFrame;

#if USE_COPYFRAME
	// set user buffer information and copied ROI
	bufframe.buf = buf;
	bufframe.rowbytes = rowbytes;
	bufframe.left = ox;
	bufframe.top = oy;
	bufframe.width = cx;
	bufframe.height = cy;

	// access image
	err = dcambuf_copyframe(hdcam, &bufframe);
	if (failed(err))
	{
		dcamcon_show_dcamerr(hdcam, err, "dcambuf_copyframe()");
		return FALSE;
	}
#else
	// access image
	err = dcambuf_lockframe(hdcam, &bufframe);
	if (failed(err))
	{
		//dcamcon_show_dcamerr(hdcam, err, "dcambuf_lockframe()");
		return FALSE;
	}

	if (bufframe.type != DCAM_PIXELTYPE_MONO16)
	{
		printf("not implement pixel type\n");
		return FALSE;
	}

	// copy target ROI
	int32 copyrowbytes = cx * 2;
	char* pSrc = (char*)bufframe.buf + oy * bufframe.rowbytes + ox * 2;
	char* pDst = (char*)buf;

	int y;
	for (y = 0; y < cy; y++)
	{
		memcpy_s(pDst, rowbytes, pSrc, copyrowbytes);

		pSrc += bufframe.rowbytes;
		pDst += rowbytes;
	}
#endif

	return TRUE;
}
const unsigned char* CHiSeq2x00Camera::GetImageBuffer()
{
	LogMessage("GetImageBuffer");
 	DCAMERR err;

	// transferinfo param
	DCAMCAP_TRANSFERINFO captransferinfo;
	memset(&captransferinfo, 0, sizeof(captransferinfo));
	captransferinfo.size = sizeof(captransferinfo);

	// get number of captured image
	err = dcamcap_transferinfo(hdcam_, &captransferinfo);

	// get image information
	int32 pixeltype = 0, width = 0, rowbytes = 0, height = 0;
	get_image_information(hdcam_, pixeltype, width, rowbytes, height);

	char x[128];
	sprintf(x, "pixelType:%i, width:%i, height:%i, rowbytes:%i, nframes:%i", pixeltype, width, height, rowbytes, captransferinfo.nFrameCount);
	LogMessage(x);

	int32 cx = width;
	int32 cy = height;
	
	unsigned char* src = new unsigned char[cx * 2 * cy];
	memset(src, 0, cx * 2 * cy);

	

	//// prepare frame param
	//DCAMBUF_FRAME bufframe;
	//memset(&bufframe, 0, sizeof(bufframe));
	//bufframe.size = sizeof(bufframe);
	//bufframe.iFrame = 0;
	//bufframe.buf = src;
	//bufframe.rowbytes = rowbytes;
	//bufframe.left = 0;
	//bufframe.top = 0;
	//bufframe.width = cx;
	//bufframe.height = cy;

	//// access image
	//err = dcambuf_copyframe(hdcam_, &bufframe);

	copy_targetarea(hdcam_, 0, src, rowbytes, 0, 0, width, height);

		
	//img_.SetPixels(src);


	dcambuf_release(hdcam_);
	dcamwait_close(hwait_);
	busy_ = false;
	LogMessage("GetImageBuffer done");
	return src;
}

MODULE_API void InitializeModuleData()
{
	// AddAvailableDeviceName(g_DeviceName, "Universal adapter using DCAM interface");
	RegisterDevice(g_DeviceName, MM::CameraDevice, "HiSeq 2000 and 2500 camera DCAM interface");
}

MODULE_API void DeleteDevice(MM::Device* pDevice)
{
	delete pDevice;
}

MODULE_API MM::Device* CreateDevice(const char* deviceName)
{
	if (deviceName == 0)
		return 0;

	string strName(deviceName);

	if (strcmp(deviceName, g_DeviceName) == 0)
		return new CHiSeq2x00Camera();

	return 0;
}