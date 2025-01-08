///
/// Copyright Krisp, Inc
///

#ifndef KRISP_AUDIO_SDK_NOISE_DB_HPP_
#define KRISP_AUDIO_SDK_NOISE_DB_HPP_

#include "krisp-audio-sdk.hpp"
#include <stdbool.h>

#ifdef __cplusplus
extern "C" {
#endif /* __cplusplus */

/*!
 * @brief This function creates Noise DB session object
 *        NoiseDB is a functionality which estimates the noise energy of a given audio fragment.
 *        In this algorithm noise energy unit is decibel (dB) which is calculated by the following formula
 *        NoiseDB=10 log(NoiseEnergy);
 *        NoiseEnergy=ni2N; 
 *
 * @param[in] inputSampleRate Sampling frequency of the input data.
 * @param[in] frameDuration Frame duration
 * @param[in] modelName The session ties to this model, and processes the future frames using it.
 *              If <b> modelName </b> is \em nullptr then the SDK auto-detects the model based on input sampleRate
 * @attention Always provide modelName explicitly to avoid ambiguity
 *
 * @return created session handle
 */
KRISP_AUDIO_API KrispAudioSessionID
krispAudioNoiseDbCreateSession(KrispAudioSamplingRate   inputSampleRate,
                               KrispAudioFrameDuration  frameDuration,
                               const char*              modelName);

/*!
 * @brief This function releases all data tied to this particular session, closes the given NoiseDB session
 *
 * @param[in] pSession Handle to the NoiseDB session to be closed
 *
 * @retval 0  success, negative on error
 */
KRISP_AUDIO_API int
krispAudioNoiseDbCloseSession(KrispAudioSessionID pSession);

/*!
 * @brief This function marks the beginning of a new audio fragment estimation 
 * 
 * @param[in] pSession Handle to the NoiseDB session
 * 
 * @retval 0  success, negative on error
 */
KRISP_AUDIO_API int
krispAudioNoiseDbResetSession(KrispAudioSessionID pSession);

/*!
 * @brief This function processes the given frame and returns the estimated noise energy in decibel (dB).
 *        For reasonably accurate results, the suggestion is to call this function on consecutive frames (at least 1 second total duration).
 *        The returned predictions are cumulative. The leaky integrator algorithm is used under the hood for
 *        aggregating the sequential frames’ noise energies.
 *
 * @param[in] pSession Handle to the NoiseDB session
 * @param[in] pFrameIn Pointer to input frame. It's a continuous buffer with overall size of <b> frameDuration * inputSampleRate / 1000</b>
 * @param[in] frameInSize This is input buffer size which must be <b> frameDuration * inputSampleRate / 1000 </b>
 *
 * @retval Estimated noise decibel (dB) value
 */
KRISP_AUDIO_API float
krispAudioNoiseDbFrameInt16(KrispAudioSessionID pSession,
                            const short*        pFrameIn,
                            unsigned int        frameInSize);

/*!
 * @brief This function processes the given frame and returns the estimated noise energy in decibel (dB).
 *        For reasonably accurate results, the suggestion is to call this function on consecutive frames (at least 1 second total duration).
 *        The returned predictions are cumulative. The leaky integrator algorithm is used under the hood for
 *        aggregating the sequential frames’ noise energies.
 *
 * @param[in] pSession Handle to the NoiseDB session
 * @param[in] pFrameIn Pointer to input frame. It's a continuous buffer with overall size of <b> frameDuration * inputSampleRate / 1000</b>
 * @param[in] frameInSize This is input buffer size which must be <b> frameDuration * inputSampleRate / 1000 </b>
 *
 * @retval Estimated noise decibel (dB) value
 */
KRISP_AUDIO_API float
krispAudioNoiseDbFrameFloat(KrispAudioSessionID pSession,
                            const float*        pFrameIn,
                            unsigned int        frameInSize);

#ifdef __cplusplus
}
#endif /* __cplusplus */

#endif //// KRISP_AUDIO_SDK_NOISE_DB_HPP_
