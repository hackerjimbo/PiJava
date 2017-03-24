CLASSPATH=/home/pi/NetBeansProjects/PiJava/build/classes
JAVAINC=-I /usr/lib/jvm/jdk-8-oracle-arm32-vfp-hflt/include -I /usr/lib/jvm/jdk-8-oracle-arm32-vfp-hflt/include/linux
RPI_WS281xDIR=/home/pi/rpi_ws281x

libjavaws2811.so: WS2811.o ${RPI_WS281xDIR}/libws2811.a
	gcc -o $@ -shared $^ -lstdc++

WS2811.o: WS2811.C Jimbo_Devices_WS2811_WS2811Raw.h ${RPI_WS281xDIR}/ws2811.h
	gcc -O -c -g -fpic ${JAVAINC} -I ${RPI_WS281xDIR} WS2811.C

Jimbo_Devices_WS2811_WS2811Raw.h:
	javah -classpath ${CLASSPATH} Jimbo.Devices.WS2811.WS2811Raw

clean:
	rm libjavaws2811.so WS2811.o Jimbo_Devices_WS2811_WS2811Raw.h

fred:
	javah ~/NetBeansProjects/PiJava/build/classes/Jimbo/Devices/WS2812/WS2811Raw
	gcc -o libws2811.so -shared WS2811Raw.o Build/rpi_ws281x/libws2811.a -lstdc++
