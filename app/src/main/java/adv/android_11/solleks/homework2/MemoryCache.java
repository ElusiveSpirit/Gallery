package adv.android_11.solleks.homework2;

import android.graphics.Bitmap;
import android.util.LruCache;

/**
 * Created by Константин on 07.11.2015.
 *
 */
public class MemoryCache extends LruCache<String, Bitmap> {
    /**
     * @param maxSize for caches that do not override {@link #sizeOf}, this is
     *                the maximum number of entries in the cache. For all other caches,
     *                this is the maximum sum of the sizes of the entries in this cache.
     */
    public MemoryCache(int maxSize) {
        super(maxSize);
    }

    @Override
    protected int sizeOf(String key, Bitmap bitmap) {
        return bitmap.getByteCount() / 1024;
    }

    public void addBitmapToMemoryCache(String key, Bitmap bitmap){
        if (getBitmapFromMemoryCache(key) == null)
            this.put(key, bitmap);
    }

    public Bitmap getBitmapFromMemoryCache(String key) {
        return this.get(key);
    }
}
