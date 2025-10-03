package com.ticketmate.backend.crypto.infrastructure.provider;

import static com.ticketmate.backend.crypto.infrastructure.constant.AesGcmConstants.IV_LEN;
import static com.ticketmate.backend.crypto.infrastructure.constant.AesGcmConstants.RNG;
import static com.ticketmate.backend.crypto.infrastructure.constant.AesGcmConstants.TAG_LEN;
import static com.ticketmate.backend.crypto.infrastructure.constant.AesGcmConstants.TRANS;

import com.ticketmate.backend.common.application.exception.CustomException;
import com.ticketmate.backend.common.application.exception.ErrorCode;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class AesGcmProvider {
  private static volatile SecretKey KEY;

  public static void initFromBase64(String base64Key) {
    if (base64Key == null || base64Key.isBlank()) {
      throw new CustomException(ErrorCode.AES_KEY_NOT_CONFIGURED);
    }
    try {
      byte[] raw = Base64.getDecoder().decode(base64Key);
      if (raw.length != 32) throw new CustomException(ErrorCode.AES_KEY_LENGTH_INVALID);
      KEY = new SecretKeySpec(raw, "AES");
    } catch (IllegalArgumentException e) {
      throw new CustomException(ErrorCode.AES_KEY_BASE64_INVALID, e);
    } catch (Exception e) {
      throw new CustomException(ErrorCode.AES_INIT_FAILED, e);
    }
  }

  public static void initFromSecretKey(SecretKey secretKey) {
    if (secretKey == null) throw new CustomException(ErrorCode.AES_KEY_NOT_CONFIGURED);
    KEY = secretKey;
  }

  public static String encrypt(String plain) {
    if (plain == null) return null;

    if (KEY == null) throw new CustomException(ErrorCode.AES_KEY_NOT_CONFIGURED);

    try {
      byte[] ivLen = new byte[IV_LEN];

      RNG.nextBytes(ivLen);

      Cipher cipher = Cipher.getInstance(TRANS);
      cipher.init(Cipher.ENCRYPT_MODE, KEY, new GCMParameterSpec(TAG_LEN, ivLen));

      byte[] ct = cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8));
      byte[] out = new byte[ivLen.length + ct.length];

      System.arraycopy(ivLen, 0, out, 0, ivLen.length);
      System.arraycopy(ct, 0, out, ivLen.length, ct.length);

      return Base64.getEncoder().encodeToString(out);

    } catch (NoSuchAlgorithmException | NoSuchPaddingException ex) {
      throw new CustomException(ErrorCode.AES_ALGO_NOT_AVAILABLE, ex);
    } catch (Exception ex) {
      throw new CustomException(ErrorCode.AES_ENCRYPT_FAILED, ex);
    }
  }

  public static String decrypt(String encB64) {
    if (encB64 == null) return null;

    if (KEY == null) throw new CustomException(ErrorCode.AES_KEY_NOT_CONFIGURED);

    try {
      byte[] all = Base64.getDecoder().decode(encB64);

      if (all.length <= IV_LEN) {
        throw new CustomException(ErrorCode.AES_CIPHERTEXT_INVALID_FORMAT);
      }

      byte[] ivLen = new byte[IV_LEN];
      byte[] ct = new byte[all.length - IV_LEN];

      System.arraycopy(all, 0, ivLen, 0, IV_LEN);
      System.arraycopy(all, IV_LEN, ct, 0, ct.length);

      Cipher cipher = Cipher.getInstance(TRANS);
      cipher.init(Cipher.DECRYPT_MODE, KEY, new GCMParameterSpec(TAG_LEN, ivLen));
      return new String(cipher.doFinal(ct), StandardCharsets.UTF_8);

    } catch (IllegalArgumentException e) { // Base64 디코드 실패
      throw new CustomException(ErrorCode.AES_CIPHERTEXT_INVALID_FORMAT, e);
    } catch (javax.crypto.AEADBadTagException e) { // 무결성 검증 실패
      throw new CustomException(ErrorCode.AES_CIPHERTEXT_TAMPERED, e);
    } catch (NoSuchAlgorithmException | NoSuchPaddingException ex) {
      throw new CustomException(ErrorCode.AES_ALGO_NOT_AVAILABLE, ex);
    } catch (Exception ex) {
      throw new CustomException(ErrorCode.AES_DECRYPT_FAILED, ex);
    }
  }
}
