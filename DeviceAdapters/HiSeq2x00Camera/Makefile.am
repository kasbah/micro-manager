AM_CXXFLAGS = -fpermissive
lib_LTLIBRARIES = libmmgr_dal_HiSeq2x00Camera.la
libmmgr_dal_HiSeq2x00Camera_la_SOURCES = HiSeq2x00Camera.cpp HiSeq2x00Camera.h ../../MMDevice/DeviceBase.h ../../MMDevice/ImgBuffer.h ../../MMDevice/DeviceUtils.h ../../MMDevice/DeviceTHreads.h
## adding the libtool library gives problems on Intel OS X 10.4, adding the static lib works
libmmgr_dal_HiSeq2x00Camera_la_LIBADD = ../../MMDevice/.libs/libMMDevice.a
##HiSeq2x00Camera_la_LDFLAGS = -module -framework dcamapi
libmmgr_dal_HiSeq2x00Camera_la_LDFLAGS = -module $(DCAMFRAMEWORKS)

install:
	cp .libs/libmmgr_dal_HiSeq2x00Camera.so $(DESTDIR)$(DEVICEADAPTERPATH)/libmmgr_dal_HiSeq2x00Camera$(MMSUFFIX)

EXTRA_DIST = HiSeq2x00Camera.vcproj license.txt

