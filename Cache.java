package ccbd.proxy.yogya;

import java.util.HashMap;
import java.util.Map;

// In memory Cache
// TODO: use 'redis' as distributed cache to cater to multiple proxy servers (for load sharing)
public class Cache {

  // httpURL is the key and byte[] is the cache value
  private static Map<String, byte[]> cache = new HashMap<>();

  /**
   * Get cached content for given GET url.
   * @param httpResourceUrl
   * @return cached content (as byte[]) or null if the url was not cached before
   */
  public static byte[] getCachedContent(String httpResourceUrl) {
    return cache.get(httpResourceUrl);
  }

  /**
   * Cache content of a given GET url.
   * @param httpResourceUrl
   * @param content byte[] to be cached against given url
   */
  public static void cacheContent(String httpResourceUrl, byte[] content) {
    System.out.println("Caching content (size = " + content.length + ") for " + httpResourceUrl);
    cache.put(httpResourceUrl, content);
  }

}
