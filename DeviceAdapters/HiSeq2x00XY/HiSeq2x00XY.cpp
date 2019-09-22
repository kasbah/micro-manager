///////////////////////////////////////////////////////////////////////////////
// FILE:          HiSeq2x00XY.cpp
// PROJECT:       Micro-Manager
// SUBSYSTEM:     DeviceAdapters
//-----------------------------------------------------------------------------
// DESCRIPTION:   Illumina HiSeq 2000 and 2500 Y motor stage adapter
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
#include "HiSeq2x00XY.h"

#include <string>

using namespace std;

const char* g_DeviceName = "HiSeq2x00XY";

CHiSeq2x00XY::CHiSeq2x00XY() :
	port_("COM18"),
	port2_("COM19"),
	initialized_(false)
{
	CPropertyAction* pAct = new CPropertyAction(this, &CHiSeq2x00XY::OnPort);
	CreateProperty(MM::g_Keyword_Port, "COM18", MM::String, false, pAct, true);
	pAct = new CPropertyAction(this, &CHiSeq2x00XY::OnPort2);
	CreateProperty("Port2", "COM19", MM::String, false, pAct, true);
}

CHiSeq2x00XY::~CHiSeq2x00XY()
{}

void CHiSeq2x00XY::GetName(char* name) const
{
	CDeviceUtils::CopyLimitedString(name, g_DeviceName);
}

int CHiSeq2x00XY::Initialize()
{
	int ret = DEVICE_OK;
	return ret;
}

int CHiSeq2x00XY::Shutdown()
{
	return DEVICE_OK;
}


bool CHiSeq2x00XY::Busy(){
	return false;
}


int CHiSeq2x00XY::OnPort(MM::PropertyBase* pProp, MM::ActionType eAct) {
	if (eAct == MM::BeforeGet) {
		pProp->Set(port_.c_str());
	}
	else if (eAct == MM::AfterSet) {
		if (initialized_)
		{
			// revert
			pProp->Set(port_.c_str());
			return DEVICE_ERR;
		}

		pProp->Get(port_);
	}
	return DEVICE_OK;
}

int CHiSeq2x00XY::OnPort2(MM::PropertyBase* pProp, MM::ActionType eAct) {
	if (eAct == MM::BeforeGet) {
		pProp->Set(port2_.c_str());
	}
	else if (eAct == MM::AfterSet) {
		if (initialized_)
		{
			// revert
			pProp->Set(port2_.c_str());
			return DEVICE_ERR;
		}

		pProp->Get(port2_);
	}
	return DEVICE_OK;
}

MODULE_API void InitializeModuleData()
{
	RegisterDevice(g_DeviceName, MM::StageDevice, "HiSeq 2000 and 2500 Y motor adapter");
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
		return new CHiSeq2x00XY();

	return 0;
}