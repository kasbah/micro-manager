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

CHiSeq2x00Camera::CHiSeq2x00Camera()
{}

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

	return ret;
}

int CHiSeq2x00Camera::Shutdown()
{
	return DEVICE_OK;
}


bool CHiSeq2x00Camera::Busy(){
	return false;
}


int CHiSeq2x00Camera::SnapImage()
{
	return DEVICE_OK;
}


const unsigned char* CHiSeq2x00Camera::GetImageBuffer()
{

	return (unsigned char*)0;
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