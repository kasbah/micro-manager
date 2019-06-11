///////////////////////////////////////////////////////////////////////////////
// FILE:          HiSeq2x00StageZ.cpp
// PROJECT:       Micro-Manager
// SUBSYSTEM:     DeviceAdapters
//-----------------------------------------------------------------------------
// DESCRIPTION:   Illumina HiSeq 2000 and 2500 Z focus stage adapter
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
#include "HiSeq2x00StageZ.h"

#include <string>

using namespace std;

const char* g_DeviceName = "HiSeq2x00StageZ";

CHiSeq2x00StageZ::CHiSeq2x00StageZ() :
	port_write_("COM16"),
	port_read_("COM15"),
	initialized_(false),
	pos_um_(0)
{
	//CPropertyAction* pActRead = new CPropertyAction(this, &CHiSeq2x00StageZ::OnPortRead);
	//CreateProperty("Port", "COM15", MM::String, false, pActRead, true);

	//CPropertyAction* pActReadBaud = new CPropertyAction(this, &CHiSeq2x00StageZ::OnPortReadBaud);
	//CreateProperty("Response Port", "115200", MM::String, false, pActRead, true);

	CPropertyAction* pActWrite = new CPropertyAction(this, &CHiSeq2x00StageZ::OnPortWrite);
	CreateProperty("Port", "COM16", MM::String, false, pActWrite, true);


	//CPropertyAction* pActWriteBaud = new CPropertyAction(this, &CHiSeq2x00StageZ::OnPortWriteBaud);
	//CreateProperty("Command Port", "115200", MM::String, false, pActWrite, true);
}

CHiSeq2x00StageZ::~CHiSeq2x00StageZ()
{}

void CHiSeq2x00StageZ::GetName(char* name) const
{
	CDeviceUtils::CopyLimitedString(name, g_DeviceName);
}

int CHiSeq2x00StageZ::Initialize()
{
	int ret = DEVICE_OK;
	LogMessage("Initialize");
	ret = GetCoreCallback()->SetSerialProperties(port_write_.c_str(), "500.0", "115200", "0.0", "Off", "None", "1");
	if (ret != 0)
	{
		return MM::CanNotCommunicate;
	}
	SetPositionUm(3000);
	/*ret = GetCoreCallback()->SetSerialProperties(port_read_.c_str(), "500.0", "115200", "0.0", "Off", "None", "1");
	if (ret != 0)
	{
		return MM::CanNotCommunicate;
	}*/
	return DEVICE_OK;
}

int CHiSeq2x00StageZ::Shutdown()
{
	return DEVICE_OK;
}


bool CHiSeq2x00StageZ::Busy(){
	return false;
}


int CHiSeq2x00StageZ::OnPortRead(MM::PropertyBase* pProp, MM::ActionType eAct) {
	if (eAct == MM::BeforeGet) {
		pProp->Set(port_read_.c_str());
	}
	else if (eAct == MM::AfterSet) {
		if (initialized_)
		{
			// revert
			pProp->Set(port_read_.c_str());
			return DEVICE_ERR;
		}

		pProp->Get(port_read_);
	}
	return DEVICE_OK;
}

int CHiSeq2x00StageZ::OnPortWrite(MM::PropertyBase* pProp, MM::ActionType eAct) {
	if (eAct == MM::BeforeGet) {
		pProp->Set(port_write_.c_str());
	}
	else if (eAct == MM::AfterSet) {
		if (initialized_)
		{
			// revert
			pProp->Set(port_write_.c_str());
			return DEVICE_ERR;
		}

		pProp->Get(port_write_);
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
		return new CHiSeq2x00StageZ();

	return 0;
}
