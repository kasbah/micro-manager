///////////////////////////////////////////////////////////////////////////////
//// FILE:       DiskoveryHub.cpp
//// PROJECT:    MicroManage
//// SUBSYSTEM:  DeviceAdapters
////-----------------------------------------------------------------------------
//// DESCRIPTION:
//// The basic ingredients for a device adapter
////                
//// AUTHOR: Nico Stuurman, 1/16/2006
////
//

#ifdef WIN32
   #include <windows.h>
   #define snprintf _snprintf 
#endif

#include "Diskovery.h"
#include <string>
#include <math.h>
#include "../../MMDevice/ModuleInterface.h"
#include <sstream>


///////////////////////////////////////////////////////////////////////////////
// Devices in this adapter.  
// The device name needs to be a class name in this file

// Diskovery device
const char* g_DiskoveryHub = "Diskovery-Hub";
const char* g_DiskoverySD = "Diskovery-Disk-Position";
const char* g_DiskoveryWF = "Diskovery-Illumination-Size";
const char* g_DiskoveryTIRF = "Diskovery-TIRF-Position";
///////////////////////////////////////////////////////////////////////////////

using namespace std;

///////////////////////////////////////////////////////////////////////////////
// Exported MMDevice API
///////////////////////////////////////////////////////////////////////////////

MODULE_API void InitializeModuleData()
{
   RegisterDevice(g_DiskoveryHub, MM::HubDevice, "Diskovery Hub (required)");
   RegisterDevice(g_DiskoverySD, MM::StateDevice, "Diskovery Spinning Disk Position");
   RegisterDevice(g_DiskoveryWF, MM::StateDevice, "Diskovery Illumination Size");
   RegisterDevice(g_DiskoveryTIRF, MM::StateDevice, "Diskovery TIRF Position");
}

MODULE_API MM::Device* CreateDevice(const char* deviceName)                  
{                                                                            
   if (deviceName == 0)                                                      
       return 0;

   if (strcmp(deviceName, g_DiskoveryHub) == 0)
   {
        return new DiskoveryHub();
   }
   if (strcmp(deviceName, g_DiskoverySD) == 0)
   {
        return new DiskoverySD();
   }
   if (strcmp(deviceName, g_DiskoveryWF) == 0)
   {
        return new DiskoveryWF();
   }
   if (strcmp(deviceName, g_DiskoveryTIRF) == 0)
   {
        return new DiskoveryStateDev(g_DiskoveryTIRF, g_DiskoveryTIRF, TIRF);
   }


   return 0;
}

MODULE_API void DeleteDevice(MM::Device* pDevice)                            
{                                                                            
   delete pDevice;                                                           
}


///////////////////////////////////////////////////////////////////////////////
// DiskoveryHub
//
DiskoveryHub::DiskoveryHub() :
   port_("Undefined"),                                                       
   model_(0),
   listener_(0),
   commander_(0),
   initialized_(false)
{
   InitializeDefaultErrorMessages();

   // create pre-initialization properties
   // ------------------------------------

   // Name
   CreateProperty(MM::g_Keyword_Name, g_DiskoveryHub, MM::String, true);
   
   // Description                                                            
   CreateProperty(MM::g_Keyword_Description, "Spinning Disk Confocal and TIRF module", MM::String, true);
 
   // Port                                                                   
   CPropertyAction* pAct = new CPropertyAction (this, &DiskoveryHub::OnPort);
   CreateProperty(MM::g_Keyword_Port, "Undefined", MM::String, false, pAct, true);

}

DiskoveryHub::~DiskoveryHub() 
{
   Shutdown();
}

int DiskoveryHub::Initialize() 
{
   model_ = new DiskoveryModel(this, *GetCoreCallback());
   listener_ = new DiskoveryListener(*this, *GetCoreCallback(), port_, model_);
   commander_ = new DiskoveryCommander(*this, *GetCoreCallback(), port_, model_);
   listener_->Start();
   RETURN_ON_MM_ERROR( commander_->Initialize() );

   // Create properties storing information from the device 
  
   // Hardware version
   CPropertyAction *pAct = new CPropertyAction (this, &DiskoveryHub::OnHardwareVersion);
   int nRet = CreateStringProperty(model_->hardwareVersionProp_, 
         model_->GetHardwareVersion().c_str(), true, pAct);
   assert(nRet == DEVICE_OK);

   // Firmware version
   pAct = new CPropertyAction (this, &DiskoveryHub::OnFirmwareVersion);
   nRet = CreateStringProperty(model_->firmwareVersionProp_, 
         model_->GetFirmwareVersion().c_str(), true, pAct);
   assert(nRet == DEVICE_OK);

   // Manufacturing date
   pAct = new CPropertyAction (this, &DiskoveryHub::OnManufacturingDate);
   nRet = CreateStringProperty(model_->manufacturingDateProp_, 
         "", true, pAct);

   // Serial Number
   pAct = new CPropertyAction (this, &DiskoveryHub::OnSerialNumber);
   nRet = CreateStringProperty(model_->serialNumberProp_, 
         model_->GetSerialNumber().c_str(), true, pAct);

   // Filter preset position
   pAct = new CPropertyAction(this, &DiskoveryHub::OnFilter);
   nRet = CreateIntegerProperty(model_->filterPositionProp_, 
         model_->GetPresetFilter(), false, pAct);
   for (int i=1; i <=4; i++) 
   {
      std::ostringstream os;
      os << i;
      AddAllowedValue(model_->filterPositionProp_, os.str().c_str());
   }

   // Iris position
   pAct = new CPropertyAction(this, &DiskoveryHub::OnIris);
   nRet = CreateIntegerProperty(model_->irisPositionProp_, 
         model_->GetPresetIris(), false, pAct);
   for (int i=1; i <=4; i++) 
   {
      std::ostringstream os;
      os << i;
      AddAllowedValue(model_->irisPositionProp_, os.str().c_str());
   }

   // TIRF position
   pAct = new CPropertyAction(this, &DiskoveryHub::OnTIRF);
   nRet = CreateIntegerProperty(model_->tirfPositionProp_, 
         model_->GetPresetTIRF(), false, pAct);
   for (int i=0; i <=5; i++) 
   {
      std::ostringstream os;
      os << i;
      AddAllowedValue(model_->tirfPositionProp_, os.str().c_str());
   }

   // motor running
   pAct = new CPropertyAction(this, &DiskoveryHub::OnMotorRunning);
   nRet = CreateIntegerProperty(model_->motorRunningProp_, 
         model_->GetMotorRunningSD(), false, pAct);
   SetPropertyLimits(model_->motorRunningProp_, 0, 1);

   initialized_ = true;
   return DEVICE_OK;
}

int DiskoveryHub::Shutdown() 
{
   if (listener_ != 0)
   {
      // speed up exciting by sending the stop signal
      // and sending a query command to the diskovery so that the
      // listener can exit
      listener_->Stop();
      commander_->GetProductModel();
      delete(listener_);
      listener_ = 0;
   }
   if (commander_ != 0)
   {
      delete(commander_);
      commander_ = 0;
   }
   if (model_ != 0)
   {
      delete(model_);
      model_ = 0;
   }
   initialized_ = false;
   return DEVICE_OK;
}

void DiskoveryHub::GetName (char* name) const
{
   CDeviceUtils::CopyLimitedString(name, g_DiskoveryHub);
}

bool DiskoveryHub::Busy() 
{
   if (model_ != 0)
   {
      return model_->GetBusy();
   }
   return false;
}

MM::DeviceDetectionStatus DiskoveryHub::DetectDevice(void )
{
   char answerTO[MM::MaxStrLength];

   if (initialized_)
      return MM::CanCommunicate;

   MM::DeviceDetectionStatus result = MM::Misconfigured;
   
   try
   {
      std::string portLowerCase = port_;
      for( std::string::iterator its = portLowerCase.begin(); its != portLowerCase.end(); ++its)
      {                                                       
         *its = (char)tolower(*its);                          
      }                                                       
      if( 0< portLowerCase.length() &&  0 != portLowerCase.compare("undefined")  && 0 != portLowerCase.compare("unknown") )
      { 
         result = MM::CanNotCommunicate;
         // record the default answer time out
         GetCoreCallback()->GetDeviceProperty(port_.c_str(), "AnswerTimeout", answerTO);

         // device specific default communication parameters
         GetCoreCallback()->SetDeviceProperty(port_.c_str(), MM::g_Keyword_Handshaking, "Off");
         GetCoreCallback()->SetDeviceProperty(port_.c_str(), MM::g_Keyword_BaudRate, "115200" );
         GetCoreCallback()->SetDeviceProperty(port_.c_str(), MM::g_Keyword_StopBits, "1");
         GetCoreCallback()->SetDeviceProperty(port_.c_str(), "AnswerTimeout", "100.0");
         GetCoreCallback()->SetDeviceProperty(port_.c_str(), "DelayBetweenCharsMs", "0");
         // Attempt to communicathe through the port
         MM::Device* pS = GetCoreCallback()->GetDevice(this, port_.c_str());
         pS->Initialize();
         PurgeComPort(port_.c_str());
         int v = 0;
         bool present = false;
         int ret = IsControllerPresent(port_, present);
         if (ret != DEVICE_OK)
            return result;
         if (present)
         {
            result = MM::CanCommunicate;
            // set the timeout to a value higher than the heartbeat ferquency
            // so that the logs will not overflow with errors
            GetCoreCallback()->SetDeviceProperty(port_.c_str(), 
                  "AnswerTimeout", "6000");
            // TODO: detected the devices behind this hub
         } else
         {
            GetCoreCallback()->SetDeviceProperty(port_.c_str(), "AnswerTimeout", answerTO);
         }
         pS->Shutdown();
      }
   }
   catch(...)
   {
      LogMessage("Exception in DetectDevice!",false);
   }

   return result;
}

/**
 * Simple way to see if the Diskovery is attached to the serial port
 */
int DiskoveryHub::IsControllerPresent(const std::string port, bool& present)
{
   present = false;
   // device seems to generate some garbage on opening port
   CDeviceUtils::SleepMs(100);
   RETURN_ON_MM_ERROR( PurgeComPort(port.c_str()) );
   // strange, this sleep really helps successful device detection!
   CDeviceUtils::SleepMs(50);
   RETURN_ON_MM_ERROR( SendSerialCommand(port.c_str(), "Q:PRODUCT_MODEL", 
            "\r\n") );
   std::string answer;
   RETURN_ON_MM_ERROR( GetSerialAnswer(port.c_str(), "\r\n", answer) );
   while (answer == "STATUS=1")
      RETURN_ON_MM_ERROR( GetSerialAnswer(port.c_str(), "\r\n", answer) );
   if (answer == "PRODUCT_MODEL=DISKOVERY")
      present = true;

   return DEVICE_OK;
}

int DiskoveryHub::DetectInstalledDevices()
{
   if (MM::CanCommunicate == DetectDevice() )
   {
      std::vector<std::string> peripherals;
      peripherals.clear();
      // TODO: actually detect devices
      peripherals.push_back(g_DiskoverySD);
      peripherals.push_back(g_DiskoveryWF);
      peripherals.push_back(g_DiskoveryTIRF);
      for (size_t i=0; i < peripherals.size(); i++) 
      {
         MM::Device* pDev = ::CreateDevice(peripherals[i].c_str());
         if (pDev)
         {
            AddInstalledDevice(pDev);
         }
      }
   }
   return DEVICE_OK;
}

///////////////////////////////////////////////////////////////////////////////
// Action handlers
///////////////////////////////////////////////////////////////////////////////

int DiskoveryHub::OnPort(MM::PropertyBase* pProp, MM::ActionType eAct)
{
   if (eAct == MM::BeforeGet) {
      pProp->Set(port_.c_str());
   } else if (eAct == MM::AfterSet) {
      pProp->Get(port_);
   }

   return DEVICE_OK;
}


int DiskoveryHub::OnHardwareVersion(MM::PropertyBase* pProp, MM::ActionType eAct)
{
   if (eAct == MM::BeforeGet) {
      pProp->Set(model_->GetHardwareVersion().c_str());
   }
   
   return DEVICE_OK;
}

int DiskoveryHub::OnFirmwareVersion(MM::PropertyBase* pProp, MM::ActionType eAct)
{
   if (eAct == MM::BeforeGet) {
      pProp->Set(model_->GetFirmwareVersion().c_str());
   }
   
   return DEVICE_OK;
}

int DiskoveryHub::OnManufacturingDate(MM::PropertyBase* pProp, MM::ActionType eAct) 
{
   if (eAct == MM::BeforeGet) {
      if (manufacturingDate_ == "") 
      {
         std::ostringstream oss3;
         oss3 << "20" << model_->GetManufactureYear() << "-" 
               << model_->GetManufactureMonth() << "-" 
               << model_->GetManufactureDay();
         manufacturingDate_ = oss3.str();
      }
      pProp->Set(manufacturingDate_.c_str());
   }
   
   return DEVICE_OK;
}

int DiskoveryHub::OnSerialNumber(MM::PropertyBase* pProp, MM::ActionType eAct)
{
   if (eAct == MM::BeforeGet) {
      pProp->Set(model_->GetSerialNumber().c_str());
   }
   
   return DEVICE_OK;
}

int DiskoveryHub::OnFilter(MM::PropertyBase* pProp, MM::ActionType eAct)
{
   if (eAct == MM::BeforeGet) {
      pProp->Set((long) model_->GetPresetFilter() );
   }
   else if (eAct == MM::AfterSet) {
      long tmp;
      pProp->Get(tmp);
      RETURN_ON_MM_ERROR( commander_->SetPresetFilter( (uint16_t) tmp) );
   }
   
   return DEVICE_OK;
}

int DiskoveryHub::OnIris(MM::PropertyBase* pProp, MM::ActionType eAct)
{
   if (eAct == MM::BeforeGet) {
      pProp->Set((long) model_->GetPresetIris() );
   }
   else if (eAct == MM::AfterSet) {
      long tmp;
      pProp->Get(tmp);
      RETURN_ON_MM_ERROR( commander_->SetPresetIris( (uint16_t) tmp) );
   }
   
   return DEVICE_OK;
}

int DiskoveryHub::OnTIRF(MM::PropertyBase* pProp, MM::ActionType eAct)
{
   if (eAct == MM::BeforeGet) {
      pProp->Set((long) model_->GetPresetTIRF() );
   }
   else if (eAct == MM::AfterSet) {
      long tmp;
      pProp->Get(tmp);
      RETURN_ON_MM_ERROR( commander_->SetPresetTIRF( (uint16_t) tmp) );
   }
   
   return DEVICE_OK;
}

int DiskoveryHub::OnMotorRunning(MM::PropertyBase* pProp, MM::ActionType eAct)
{
   if (eAct == MM::BeforeGet) {
      pProp->Set((long) model_->GetMotorRunningSD() );
   }
   else if (eAct == MM::AfterSet) {
      long tmp;
      pProp->Get(tmp);
      RETURN_ON_MM_ERROR( commander_->SetMotorRunningSD( (uint16_t) tmp) );
   }
   
   return DEVICE_OK;
}






// Device communication functions

int DiskoveryHub::QueryCommand(const char* command, std::string& answer) 
{
   RETURN_ON_MM_ERROR (PurgeComPort(port_.c_str()));
   RETURN_ON_MM_ERROR (SendSerialCommand(port_.c_str(), command, "\n"));
   RETURN_ON_MM_ERROR (GetSerialAnswer(port_.c_str(), "\r\n", answer));
   std::vector<std::string> tokens = split(answer, '=');
   if (tokens.size() == 2) {
      answer = tokens[1];
      return DEVICE_OK;
   }
   // TODO find appropriate error code
   return 1;
}

int DiskoveryHub::QueryCommandInt(const char* command, unsigned int* result) 
{
   RETURN_ON_MM_ERROR (PurgeComPort(port_.c_str()));
   RETURN_ON_MM_ERROR (SendSerialCommand(port_.c_str(), command, "\n"));
   // even though the documentation states that an integer is returned, in real life
   // the device returns a string that ends with "=#", where "#" is what we want
   std::string answer;
   RETURN_ON_MM_ERROR (GetSerialAnswer(port_.c_str(), "\r\n", answer));
   std::vector<std::string> tokens = split(answer, '=');
   if (tokens.size() == 2) {
      istringstream(tokens[1].c_str()) >> *result;
      return DEVICE_OK;
   }
   // TODO find appropriate error code
   return 1;
}

std::vector<std::string>& DiskoveryHub::split(const std::string &s, char delim, std::vector<std::string> &elems) {
    std::stringstream ss(s);
    std::string item;
    while (std::getline(ss, item, delim)) {
        elems.push_back(item);
    }
    return elems;
}

std::vector<std::string> DiskoveryHub::split(const std::string &s, char delim) {
    std::vector<std::string> elems;
    split(s, delim, elems);
    return elems;
}

///////////////////////////////////////////////////////////////////////////////
// DiskoverStateDevy
//
DiskoveryStateDev::DiskoveryStateDev(
      const std::string devName, const std::string description, const DevType devType)  :
   devName_(devName),
   devType_ (devType),
   initialized_(false),
   hub_(0)
{
   firstPos_ = 1;
   numPos_ = 5;
   if (devType_ == WF) 
   {
      numPos_ = 4;
   }
   if (devType_ == TIRF) 
   {
      firstPos_ = 0;
   }

   InitializeDefaultErrorMessages();

   // Description
   int ret = CreateProperty(MM::g_Keyword_Description, description.c_str(), MM::String, true);
   assert(DEVICE_OK == ret);

   // Name
   ret = CreateProperty(MM::g_Keyword_Name, devName_.c_str(), MM::String, true);
   assert(DEVICE_OK == ret);

   // parent ID display
   CreateHubIDProperty();
}

DiskoveryStateDev::~DiskoveryStateDev() 
{
   Shutdown();
}

int DiskoveryStateDev::Shutdown()
{
   initialized_ = false;
   hub_ = 0;
   return DEVICE_OK;
}

void DiskoveryStateDev::GetName(char* name) const
{
   CDeviceUtils::CopyLimitedString(name, devName_.c_str());
}

int DiskoveryStateDev::Initialize() 
{
   hub_ = static_cast<DiskoveryHub*>(GetParentHub());
   if (!hub_)
      return DEVICE_COMM_HUB_MISSING;
   char hubLabel[MM::MaxStrLength];
   hub_->GetLabel(hubLabel);
   SetParentID(hubLabel); // for backward compatibility (delete?)

   for (unsigned int i = 0; i < numPos_; i++) {
      ostringstream os;
      os << "Preset-" << (i + firstPos_);
      SetPositionLabel(i, os.str().c_str());
   }

   // State
   // -----
   CPropertyAction* pAct = new CPropertyAction (this, &DiskoveryStateDev::OnState);
   int nRet = CreateProperty(MM::g_Keyword_State, "0", MM::Integer, false, pAct);
   if (nRet != DEVICE_OK)
      return nRet;
   for (int i = 0; i < numPos_; i++) 
   {
      std::ostringstream os;
      os << i;
      AddAllowedValue(MM::g_Keyword_State, os.str().c_str());
   }

   // Label
   // -----
   pAct = new CPropertyAction (this, &CStateBase::OnLabel);
   nRet = CreateProperty(MM::g_Keyword_Label, "", MM::String, false, pAct);
   if (nRet != DEVICE_OK)
      return nRet;

   // Register our instance for callbacks
   if (devType_ == SD)
      hub_->RegisterSDDevice(this);
   else if (devType_ == WF)
      hub_->RegisterWFDevice(this);
   else if (devType_ == TIRF)
      hub_->RegisterTIRFDevice(this);

   return DEVICE_OK;
}

bool DiskoveryStateDev::Busy() 
{
   if (hub_ != 0)
   {
      return hub_->Busy();
   }
   return false;
}

int DiskoveryStateDev::OnState(MM::PropertyBase* pProp, MM::ActionType eAct)
{
   if (devType_ == SD)
   {
      if (eAct == MM::BeforeGet)
      {
         pProp->Set( (long) (hub_->GetModel()->GetPresetSD() - 1) );
      }
      else if (eAct == MM::AfterSet)
      {
         long pos;
         pProp->Get(pos);
         hub_->GetCommander()->SetPresetSD( (uint16_t) (pos + 1));
      }
   } 
   else if (devType_ == WF)
   {
      if (eAct == MM::BeforeGet)
      {
         pProp->Set( (long) (hub_->GetModel()->GetPresetWF() - firstPos_) );
      }
      else if (eAct == MM::AfterSet)
      {
         long pos;
         pProp->Get(pos);
         hub_->GetCommander()->SetPresetWF( (uint16_t) (pos + firstPos_));
      }
   }
   else if (devType_ == TIRF)
   {
      if (eAct == MM::BeforeGet)
      {
         pProp->Set( (long) (hub_->GetModel()->GetPresetTIRF() - firstPos_) );
      }
      else if (eAct == MM::AfterSet)
      {
         long pos;
         pProp->Get(pos);
         hub_->GetCommander()->SetPresetTIRF( (uint16_t) (pos + firstPos_));
      }
   }
   return DEVICE_OK;
}

///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
// DiskoverySD
//
DiskoverySD::DiskoverySD()  :
   initialized_(false),
   hub_(0)
{
   InitializeDefaultErrorMessages();

   // Description
   int ret = CreateProperty(MM::g_Keyword_Description, "Diskovery Spinning Disk Position", MM::String, true);
   assert(DEVICE_OK == ret);

   // Name
   ret = CreateProperty(MM::g_Keyword_Name, "Diskovery Spinning Disk", MM::String, true);
   assert(DEVICE_OK == ret);

   // parent ID display
   CreateHubIDProperty();
}

DiskoverySD::~DiskoverySD() 
{
   Shutdown();
}

int DiskoverySD::Shutdown()
{
   initialized_ = false;
   hub_ = 0;
   return DEVICE_OK;
}

void DiskoverySD::GetName(char* name) const
{
   CDeviceUtils::CopyLimitedString(name, g_DiskoverySD);
}

int DiskoverySD::Initialize() 
{
   hub_ = static_cast<DiskoveryHub*>(GetParentHub());
   if (!hub_)
      return DEVICE_COMM_HUB_MISSING;
   char hubLabel[MM::MaxStrLength];
   hub_->GetLabel(hubLabel);
   SetParentID(hubLabel); // for backward compatibility (delete?)

   for (unsigned int i = 0; i < NUMPOS; i++) {
      ostringstream os;
      os << (i + 1);
      SetPositionLabel(i, os.str().c_str());
   }

   // State
   // -----
   CPropertyAction* pAct = new CPropertyAction (this, &DiskoverySD::OnState);
   int nRet = CreateProperty(MM::g_Keyword_State, "0", MM::Integer, false, pAct);
   if (nRet != DEVICE_OK)
      return nRet;
   for (int i=0; i < NUMPOS; i++) 
   {
      std::ostringstream os;
      os << i;
      AddAllowedValue(MM::g_Keyword_State, os.str().c_str());
   }

   // Label

   // Label
   // -----
   pAct = new CPropertyAction (this, &CStateBase::OnLabel);
   nRet = CreateProperty(MM::g_Keyword_Label, "", MM::String, false, pAct);
   if (nRet != DEVICE_OK)
      return nRet;

   // Register our instance for callbacks
   hub_->RegisterSDDevice(this);

   return DEVICE_OK;
}

bool DiskoverySD::Busy() 
{
   if (hub_ != 0)
   {
      return hub_->Busy();
   }
   return false;
}

int DiskoverySD::OnState(MM::PropertyBase* pProp, MM::ActionType eAct)
{
   if (eAct == MM::BeforeGet)
   {
      pProp->Set( (long) (hub_->GetModel()->GetPresetSD() - 1) );
   }
   else if (eAct == MM::AfterSet)
   {
      long pos;
      pProp->Get(pos);
      hub_->GetCommander()->SetPresetSD( (uint16_t) (pos + 1));
   }
   return DEVICE_OK;
}

///////////////////////////////////////////////////////////////////////////////
// DiskoveryWF
//
DiskoveryWF::DiskoveryWF()  :
   initialized_(false),
   hub_(0)
{
   InitializeDefaultErrorMessages();

   // Description
   int ret = CreateProperty(MM::g_Keyword_Description, "Diskovery IlluminationSize", MM::String, true);
   assert(DEVICE_OK == ret);

   // Name
   ret = CreateProperty(MM::g_Keyword_Name, "Diskovery Illumination Size", MM::String, true);
   assert(DEVICE_OK == ret);

   // parent ID display
   CreateHubIDProperty();
}

DiskoveryWF::~DiskoveryWF() 
{
   Shutdown();
}

int DiskoveryWF::Shutdown()
{
   initialized_ = false;
   hub_ = 0;
   return DEVICE_OK;
}

void DiskoveryWF::GetName(char* name) const
{
   CDeviceUtils::CopyLimitedString(name, g_DiskoveryWF);
}

int DiskoveryWF::Initialize() 
{
   hub_ = static_cast<DiskoveryHub*>(GetParentHub());
   if (!hub_)
      return DEVICE_COMM_HUB_MISSING;
   char hubLabel[MM::MaxStrLength];
   hub_->GetLabel(hubLabel);
   SetParentID(hubLabel); // for backward compatibility (delete?)

   for (unsigned int i = 0; i < NUMPOS; i++) {
      ostringstream os;
      os << (i + 1);
      SetPositionLabel(i, os.str().c_str());
   }

   // State
   // -----
   CPropertyAction* pAct = new CPropertyAction (this, &DiskoveryWF::OnState);
   int nRet = CreateProperty(MM::g_Keyword_State, "0", MM::Integer, false, pAct);
   if (nRet != DEVICE_OK)
      return nRet;
   for (int i=0; i < NUMPOS; i++) 
   {
      std::ostringstream os;
      os << i;
      AddAllowedValue(MM::g_Keyword_State, os.str().c_str());
   }

   // Label
   // -----
   pAct = new CPropertyAction (this, &CStateBase::OnLabel);
   nRet = CreateProperty(MM::g_Keyword_Label, "", MM::String, false, pAct);
   if (nRet != DEVICE_OK)
      return nRet;

   // Register our instance for callbacks
   hub_->RegisterWFDevice(this);

   return DEVICE_OK;
}

bool DiskoveryWF::Busy() 
{
   if (hub_ != 0)
   {
      return hub_->Busy();
   }
   return false;
}

int DiskoveryWF::OnState(MM::PropertyBase* pProp, MM::ActionType eAct)
{
   if (eAct == MM::BeforeGet)
   {
      pProp->Set( (long) (hub_->GetModel()->GetPresetWF() - FIRSTPOS) );
   }
   else if (eAct == MM::AfterSet)
   {
      long pos;
      pProp->Get(pos);
      hub_->GetCommander()->SetPresetWF( (uint16_t) (pos + FIRSTPOS));
   }
   return DEVICE_OK;
}

