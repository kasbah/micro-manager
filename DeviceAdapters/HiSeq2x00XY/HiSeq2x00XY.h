///////////////////////////////////////////////////////////////////////////////
// FILE:          HiSeq2x00XY.h
// PROJECT:       Micro-Manager
// SUBSYSTEM:     DeviceAdapters
//-----------------------------------------------------------------------------
// DESCRIPTION:   Illumina HiSeq 2000 and 2500 X and Y motor stage adapter
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



class CHiSeq2x00XY : public CXYStageBase<CHiSeq2x00XY>
{
public:
	CHiSeq2x00XY();
	~CHiSeq2x00XY();
	// MMDevice API
	int Initialize();
	int Shutdown();

	void GetName(char* name) const;
	bool Busy();

	int SetPositionUmY(double pos) { 
		std::ostringstream command;
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


		ret = SendSerialCommand(port_.c_str(), "1G", "\r");
		if (ret != DEVICE_OK) {
			return ret;
		}
		ret = GetSerialAnswer(port_.c_str(), "\r\n", throw_away);
		if (ret != DEVICE_OK) {
			return ret;
		}		
		return DEVICE_OK; 
	
	};
	int SetPositionUmX(double pos) {
		std::ostringstream command;
		std::string throw_away;
		command << "MA " << int(pos * 100);
		int ret = SendSerialCommand(port2_.c_str(), command.str().c_str(), "\r");
		if (ret != DEVICE_OK) {
			return ret;
		}
		ret = GetSerialAnswer(port2_.c_str(), "\r\n", throw_away);
		if (ret != DEVICE_OK) {
			return ret;
		}
		return DEVICE_OK;

	}
	int GetPositionUmY(double& pos) { 
		std::string answer;
		std::string throw_away;
		
		int ret = SendSerialCommand(port_.c_str(), "1D", "\r");
		if (ret != DEVICE_OK) {
			return ret;
		}
		ret = GetSerialAnswer(port_.c_str(), "\r\n", throw_away);
		ret = GetSerialAnswer(port_.c_str(), "\r\n", answer);

		if (ret != DEVICE_OK) {
			return ret;
		}
		
		// answer is in the form: "*-3000" so we ignore the "*" and convert to float
		pos = atof(answer.erase(0, 1).c_str()) / 100;

		return DEVICE_OK; 
	};
	int GetPositionUmX(double& pos) {
		std::string answer;
		std::string throw_away;

		int ret = SendSerialCommand(port2_.c_str(), "PR P", "\r");
		if (ret != DEVICE_OK) {
			return ret;
		}
		ret = GetSerialAnswer(port2_.c_str(), "\r\n", throw_away);
		if (ret != DEVICE_OK) {
			return ret;
		}
		ret = GetSerialAnswer(port2_.c_str(), "\r\n", answer);

		if (ret != DEVICE_OK) {
			return ret;
		}

		pos = atof(answer.c_str()) / 100;

		return DEVICE_OK;
	}
	//int SetOrigin() { return DEVICE_OK; };
	//int GetLimits(double& min, double& max) { return DEVICE_OK; };
	//double GetStepSize() { return 0; }
	//int SetPositionSteps(long steps) { return DEVICE_OK; };
	//int GetPositionSteps(long& steps) { return DEVICE_OK; };
	//int IsStageSequenceable(bool& isSequenceable) const { isSequenceable = false; return DEVICE_OK; }
	//bool IsContinuousFocusDrive(void) const { return false; };

	// XYStage API
// -----------
	int SetPositionSteps(long x, long y)
	{
		int ret = SetPositionUmX(x);
		if (ret != DEVICE_OK) {
			return ret;
		}
		return SetPositionUmY(y);
	}
	//int SetRelativePositionSteps(long x, long y)
	//{
	//	return DEVICE_OK;
	//}
	int GetPositionSteps(long& x, long& y)
	{
		double posX;
		double posY;
		GetPositionUmX(posX);
		GetPositionUmY(posY);
		char msg[64];
		sprintf(msg, "X: %f", posX);
		LogMessage(msg);
		sprintf(msg, "Y: %f", posY);
		LogMessage(msg);
		x = long(posX);
		y = long(posY);
		return DEVICE_OK;
	}
	int Home()
	{
		return DEVICE_OK;
	}
	int Stop()
	{
		return DEVICE_OK;
	}
	int SetOrigin()
	{
		return DEVICE_OK;
	}
	int GetLimitsUm(double& xMin, double& xMax, double& yMin, double& yMax)
	{
		return DEVICE_UNSUPPORTED_COMMAND;
	}
	int GetStepLimits(long& xMin, long& xMax, long& yMin, long& yMax)
	{
		return DEVICE_UNSUPPORTED_COMMAND;
	}
	double GetStepSizeXUm() { return 1; }
	double GetStepSizeYUm() { return 1; }
	int IsXYStageSequenceable(bool& isSequenceable) const { isSequenceable = false; return DEVICE_OK; }


	int OnPort(MM::PropertyBase* pProp, MM::ActionType eAct);
	int OnPort2(MM::PropertyBase* pProp, MM::ActionType eAct);

private:
	double pos_um_;
	std::string port_;
	std::string port2_;
	bool initialized_;
	double stepSizeXUm_;
	double stepSizeYUm_;

};


#endif //_HISEQ2X00MOTORY_H_
