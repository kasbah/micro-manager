///////////////////////////////////////////////////////////////////////////////
// FILE:          HiSeq2x00MotorY.h
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

#ifndef _HISEQ2X00MOTORY_H_
#define _HISEQ2X00MOTORY_H_

#include "../../MMDevice/DeviceBase.h"

class CHiSeq2x00MotorY : public CStageBase<CHiSeq2x00MotorY>
{
public:
	CHiSeq2x00MotorY();
	~CHiSeq2x00MotorY();
	// MMDevice API
	int Initialize();
	int Shutdown();

	void GetName(char* name) const;
	bool Busy();

	int SetPositionUm(double pos) { 
		std::ostringstream command;
		std::ostringstream command2;
		std::string throw_away;
		command << "1D" << int(pos * 100);
		int ret = SendSerialCommand(port_.c_str(), command.str().c_str(), "\r");
		if (ret != DEVICE_OK) {
			return ret;
		}

		ret = GetSerialAnswer(port_.c_str(), "\r\n", throw_away);
		if (ret != DEVICE_OK) {
			return ret;
		}

		command2 << "1G";
		ret = SendSerialCommand(port_.c_str(), command2.str().c_str(), "\r");
		if (ret != DEVICE_OK) {
			return ret;
		}
		ret = GetSerialAnswer(port_.c_str(), "\r\n", throw_away);
		if (ret != DEVICE_OK) {
			return ret;
		}		
		return DEVICE_OK; 
	
	};
	int GetPositionUm(double& pos) { 
		std::ostringstream command;
		std::string answer;
		std::string throw_away;
		command << "1D";
		int ret = SendSerialCommand(port_.c_str(), command.str().c_str(), "\r");
		if (ret != DEVICE_OK) {
			return ret;
		}
		ret = GetSerialAnswer(port_.c_str(), "\r\n", throw_away);
		ret = GetSerialAnswer(port_.c_str(), "\r\n", answer);

		if (ret != DEVICE_OK) {
			return ret;
		}
		
		pos = atof(answer.erase(0, 1).c_str()) / 100;

		return DEVICE_OK; 
	};
	int SetOrigin() { return DEVICE_OK; };
	int GetLimits(double& min, double& max) { return DEVICE_OK; };
	double GetStepSize() { return 0; }
	int SetPositionSteps(long steps) { return DEVICE_OK; };
	int GetPositionSteps(long& steps) { return DEVICE_OK; };
	int IsStageSequenceable(bool& isSequenceable) const { isSequenceable = false; return DEVICE_OK; }
	bool IsContinuousFocusDrive(void) const { return false; };


	int OnPort(MM::PropertyBase* pProp, MM::ActionType eAct);

private:
	double pos_um_;
	std::string port_;
	bool initialized_;

};


#endif //_HISEQ2X00MOTORY_H_
