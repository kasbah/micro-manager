AM_CXXFLAGS = -fpermissive
lib_LTLIBRARIES = libmmgr_dal_HiSeq2x00StageZ.la
libmmgr_dal_HiSeq2x00StageZ_la_SOURCES = HiSeq2x00StageZ.cpp HiSeq2x00StageZ.h ../../MMDevice/DeviceBase.h ../../MMDevice/ImgBuffer.h ../../MMDevice/DeviceUtils.h ../../MMDevice/DeviceTHreads.h
## adding the libtool library gives problems on Intel OS X 10.4, adding the static lib works
libmmgr_dal_HiSeq2x00StageZ_la_LIBADD = ../../MMDevice/.libs/libMMDevice.a
##HiSeq2x00StageZ_la_LDFLAGS = -module -framework dcamapi
libmmgr_dal_HiSeq2x00StageZ_la_LDFLAGS = -module $(DCAMFRAMEWORKS)

install:
	cp .libs/libmmgr_dal_HiSeq2x00StageZ.so $(DESTDIR)$(DEVICEADAPTERPATH)/libmmgr_dal_HiSeq2x00StageZ$(MMSUFFIX)

EXTRA_DIST = HiSeq2x00StageZ.vcproj license.txt

