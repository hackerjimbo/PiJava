#include <iostream>
#include <stdint.h>

extern "C" {
#include <ws2811.h>
}

#include "Jimbo_Devices_WS2811_WS2811Raw.h"

namespace
{
  bool in_use (false);

  const int TARGET_FREQ (WS2811_TARGET_FREQ);
  const int GPIO_PIN (18);
  const int DMA (5);

  ws2811_t leds;
}

#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     WS2811_WS2811
 * Method:    ws2811_init
 * Signature: (I)Z
 */
JNIEXPORT jboolean JNICALL Java_Jimbo_Devices_WS2811_WS2811Raw_ws2811_1init
  (JNIEnv *env, jclass c, jint type, jint length)
{
  if (in_use)
    return JNI_FALSE;

  // std::cout << "In C++ land, type = " << type
  //	    << " length = " << length
  //	    << std::endl;

  leds.freq = TARGET_FREQ;
  leds.dmanum = DMA;
  leds.channel[0].gpionum = GPIO_PIN;
  leds.channel[0].count = length;
  leds.channel[0].invert = 0;
  leds.channel[0].brightness = 255;
  leds.channel[0].strip_type = type;
  leds.channel[1].gpionum = 0;
  leds.channel[1].count = 0;
  leds.channel[1].invert = 0;
  leds.channel[1].brightness = 0;

  if (ws2811_init (&leds) != WS2811_SUCCESS)
    return JNI_FALSE;

  in_use = true;

  return JNI_TRUE;
}

/*
 * Class:     Jimbo_Devices_WS2811_WS2811Raw
 * Method:    ws2811_brightness
 * Signature: (I)Z
 */

JNIEXPORT jboolean JNICALL Java_Jimbo_Devices_WS2811_WS2811Raw_ws2811_1brightness
  (JNIEnv *env, jclass c, jint value)
{
  if (value < 0 || value > 255)
  {
    std::cerr << "WS2811 brightness set to " << value << std::endl;
    return JNI_FALSE;
  }
  
  leds.channel[0].brightness = value;
  return JNI_TRUE;
}

/*
 * Class:     WS2811_WS2811
 * Method:    ws2811_update
 * Signature: ([I)Z
 */
JNIEXPORT jboolean JNICALL Java_Jimbo_Devices_WS2811_WS2811Raw_ws2811_1update
  (JNIEnv *env, jclass c, jintArray jdata)
{
  if (!in_use)
    return JNI_FALSE;
  
  jboolean copy;

  const jint len = env->GetArrayLength (jdata);

  // std::cout << "Update with " << len << " items" << std::endl;

  if (len != leds.channel[0].count)
    return JNI_FALSE;
  
  jint *raw = env->GetIntArrayElements (jdata, &copy);

  // std::cout << "Got the data, copy is " << (int) copy << std::endl;
  // std::cout << "Data at " << leds.channel[0].leds << std::endl;
  
  for (int i = 0; i < len; ++i)
    leds.channel[0].leds[i] = raw[i];

  // std::cout << "Releasing elements" << std::endl;
  
  env->ReleaseIntArrayElements (jdata, raw, 0);

  // std::cout << "Rendering" << std::endl;
  
  ws2811_render (&leds);

  // std::cout << "And we're done" << std::endl;
  
  return JNI_TRUE;
}

/*
 * Class:     WS2811_WS2811
 * Method:    ws2811_wait
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_Jimbo_Devices_WS2811_WS2811Raw_ws2811_1wait
  (JNIEnv *env, jclass c)
{
  if (!in_use)
    return JNI_FALSE;

  // std::cout << "Wait" << std::endl;

  ws2811_wait (&leds);
  
  return JNI_TRUE;
}

/*
 * Class:     WS2811_WS2811
 * Method:    ws2811_close
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_Jimbo_Devices_WS2811_WS2811Raw_ws2811_1close
  (JNIEnv *env, jclass c)
{
  const bool result = in_use;

  // std::cout << "That's all folks!" << std::endl;
  
  in_use = false;

  ws2811_fini (&leds);
  
  return result;
}

#ifdef __cplusplus
}
#endif
