///////////////////////////////////////////////////////////////////////////////
// FILE:          HiSeq2x00Camera.h
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

#ifndef _HISEQ2X00CAMERA_H_
#define _HISEQ2X00CAMERA_H_

#include "../../MMDevice/DeviceBase.h"
#include "../../MMDevice/ImgBuffer.h"

#include "../../../3rdparty/Hamamatsu/DCAMSDK/201808/inc/dcamapi4.h"
#include "../../../3rdparty/Hamamatsu/DCAMSDK/201808/inc/dcamprop.h"

class CHiSeq2x00Camera : public CCameraBase<CHiSeq2x00Camera>
{
public:
	CHiSeq2x00Camera();
	~CHiSeq2x00Camera();
	// MMDevice API
	int Initialize();
	int Shutdown();

	void GetName(char* pszName) const;
	bool Busy();

	// MMCamera API
	int SnapImage();
	const unsigned char* GetImageBuffer();
	unsigned GetImageWidth() const { return img_.Width(); }
	unsigned GetImageHeight() const { return img_.Height(); }
	unsigned GetImageBytesPerPixel() const { return img_.Depth(); }
	long GetImageBufferSize() const { return img_.Width() * img_.Height() * GetImageBytesPerPixel(); }
	unsigned GetBitDepth() const { return 0; };
	double GetExposure() const { return 0; };
	void SetExposure(double dExp) {};
	int SetROI(unsigned uX, unsigned uY, unsigned uXSize, unsigned uYSize) {
		return DEVICE_OK;
	};
	int GetROI(unsigned& uX, unsigned& uY, unsigned& uXSize, unsigned& uYSize) {
		return DEVICE_OK;
	};
	int ClearROI() {
		return DEVICE_OK;
	};
	int GetBinning() const { return lnBin_; };
	int SetBinning(int binSize) {
		return DEVICE_OK;
	};


	// high-speed interface
	int StartSequenceAcquisition(long numImages, double interval_ms, bool stopOnOverflow) {
		return DEVICE_OK;
	};
	int StopSequenceAcquisition() {
		return DEVICE_OK;
	};
	int RestartSequenceAcquisition() {
		return DEVICE_OK;
	};
	bool IsCapturing() {
		return false ;
	};
	int RestartSnapMode() {
		return DEVICE_OK;
	};
	int IsExposureSequenceable(bool& isSequenceable) const { isSequenceable = false; return DEVICE_OK; }


	// custom interface for the burst thread
	int PushImage();
private:
	ImgBuffer img_;
	long lnBin_;
	HDCAM hdcam_;
};


#endif //_HISEQ2X00CAMERA_H_
