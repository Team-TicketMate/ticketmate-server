package com.ticketmate.backend.storage.infrastructure.util;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.experimental.UtilityClass;
import org.springframework.web.multipart.MultipartFile;

@UtilityClass
public class FileUtil {

  private static final Pattern PERCENT_ENCODED = Pattern.compile("(%[0-9A-Fa-f]{2})");

  /**
   * Multipartfile이 null이거나 빈 파일인지 체크
   */
  public boolean isNullOrEmpty(MultipartFile file) {
    return file == null || file.isEmpty() || file.getOriginalFilename() == null;
  }

  /**
   * 1. rawPath 정규화
   * 2. baseUrl과 결합
   * 3. Spring UriComponentsBuilder.encode()로 UTF-8 percent-encoding 수행
   *
   * @param baseUrl 기본 URL
   * @param rawPath 사용자 입력 Path
   * @return
   */
  public String buildNormalizedAndEncodedUrl(String baseUrl, String rawPath) {
    String normalizedPath = normalizePath(rawPath);
    String encodedPath = encodePathSegments(normalizedPath);
    return combineBaseAndPath(baseUrl, encodedPath);
  }

  /**
   * 사용자 입력 경로 정규화
   * 1. 앞 뒤 공백 제거
   * 2. 백슬래시 ('\')를 슬래시('/')로 변환
   * 3. 중복 슬래시("//")를 단일 슬래시로 축소
   * 4. 루트를 나타내는 "/"는 빈 문자열로 변환
   * 5. 절대 경로로 변환: 선행 슬래시('/') 추가
   * 6. 불필요한 후행 슬래시('/') 제거
   *
   * @param rawPath 사용자 입력 경로
   * @return 정규화된 경로 ("/webdav")
   */
  public static String normalizePath(String rawPath) {
    if (rawPath == null) {
      return "";
    }
    String p = rawPath.trim().replace('\\', '/');
    p = p.replaceAll("/+", "/");
    if ("/".equals(p)) {
      return "";
    }
    if (!p.startsWith("/")) {
      p = "/" + p;
    }
    if (p.endsWith("/") && p.length() > 1) {
      p = p.substring(0, p.length() - 1);
    }
    return p;
  }

  /**
   * BASE URL과 경로를 결합합니다.
   *
   * @param baseUrl WebDAV 서버의 베이스 URL (후행 슬래시 제거)
   * @param path    경로
   * @return 결합된 URL
   */
  public String combineBaseAndPath(String baseUrl, String path) {
    String base = removeTrailingSlash(baseUrl);
    if (path == null || path.isEmpty()) {
      return base;
    }
    if (!path.startsWith("/")) {
      return base + "/" + path;
    }
    return base + path;
  }

  /**
   * UTF-8 인코딩
   *
   * @param input 문자열
   * @return 인코딩 된 문자열
   */
  private static String encodeString(String input) {
    if (input == null || input.isEmpty()) {
      return "";
    }
    return URLEncoder
        .encode(input, StandardCharsets.UTF_8)
        .replace("+", "%20");
  }

  /**
   * 경로 세그먼트 중 인코딩되지 않은 부분만 인코딩
   *
   * @param path 경로
   * @return 인코딩되지 않은 부분만 인코딩 후 반환
   */
  public static String encodePathSegments(String path) {
    if (path == null || path.isEmpty()) {
      return "";
    }
    StringBuilder sb = new StringBuilder();
    String[] segments = path.split("/");
    for (String segment : segments) {
      if (segment.isEmpty()) {
        continue;
      }
      sb.append("/");
      sb.append(encodeSegmentPreservingPercents(segment));
    }
    return sb.toString();
  }

  /**
   * URL 또는 경로 문자열의 끝에 있는 슬래시('/) 제거
   *
   * @param url 슬래시 제거 대상 문자열
   * @return 후행 슬래시가 제거된 문자열
   */
  public static String removeTrailingSlash(String url) {
    if (url == null || url.isEmpty()) {
      return url;
    }
    return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
  }

  private static String encodeSegmentPreservingPercents(String segment) {
    Matcher m = PERCENT_ENCODED.matcher(segment);
    int last = 0;
    StringBuilder out = new StringBuilder();
    while (m.find()) {
      // 1) [last..m.start()) 구간은 인코딩
      String literal = segment.substring(last, m.start());
      if (!literal.isEmpty()) {
        out.append(encodeString(literal));
      }
      // 2) %XX 부분은 그대로 append
      out.append(m.group(1));
      last = m.end();
    }
    // 3) 남은 tail 구간 인코딩
    String tail = segment.substring(last);
    if (!tail.isEmpty()) {
      out.append(encodeString(tail));
    }
    return out.toString();
  }
}
