package nz.ac.massey.caigwatkin.simplegallery;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;

import java.util.ArrayList;

/**
 * Image Grid View class.
 *
 * Displays a grid view of images sourced from device folders.
 */
public class ImageGridView extends GridView {

    /**
     * The size of thumbnails to display.
     */
    static int THUMB_SIZE = 148;

    /**
     * Stores paths to images shown as thumbnails.
     */
    private final ArrayList<String> mImagePaths = new ArrayList<>();

    private BaseAdapter mAdapter;

    /**
     * Stores thumbnail bitmaps for display;
     */
    private final ArrayList<Bitmap> mThumbBitmapList = new ArrayList<>();

//    /**
//     * Constructor from context.
//     *
//     * @param context The context in which the view is created.
//     */
//    public ImageGridView(Context context) {
//        super(context);
//    }

    /**
     * Constructor from context and attributes.
     *
     * @param context The context in which the view is created.
     * @param attributeSet Attributes of the view.
     */
    public ImageGridView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setAdapter(new ImageAdapter(getContext(), mThumbBitmapList));
        mAdapter = (BaseAdapter) getAdapter();
    }

    /**
     * Initialises the view.
     *
     * Adds thumbnail image views to grid view. Sets up click listener to start new activity when thumbnail clicked.
     */
    public void init() {
        getImages();
        setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                openFullscreenImageActivity(position);
            }
        });
    }

    /**
     * Opens a fullscreen image activity.
     *
     * Starts intent to display a new fullscreen image activity based on the path to the image at position.
     *
     * @param position The position of the image from the adapter.
     */
    private void openFullscreenImageActivity(int position) {
        Intent intent = new Intent(getContext(), FullscreenImage.class);
        intent.putExtra("path", mImagePaths.get(position));
        getContext().startActivity(intent);
    }

    /**
     * Gets a all images from device as thumbnails and paths.
     */
    private void getImages() {
        final String[] columns = new String[]{ MediaStore.Images.Media.DATA };
        final String orderBy = MediaStore.Images.Media.DATE_ADDED + " DESC";
        Thread thread = new Thread(new Runnable() {
            /**
             * Attempts to load images.
             */
            @Override
            public void run() {
                try {
                    loadImages();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            /**
             * Loads images from device folders.
             *
             * Stores image paths. Creates and stores thumbnails.
             */
            private void loadImages() {
                Cursor cursor = getContext().getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        columns, null, null, orderBy);
                int length = cursor.getCount();
                for (int i = 0; i < length; i++) {
                    cursor.moveToPosition(i);
                    int dataColumnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
                    String path = cursor.getString(dataColumnIndex);
                    mImagePaths.add(i, path);
                    mThumbBitmapList.add(ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(path),
                            THUMB_SIZE, THUMB_SIZE));
                    mAdapter.notifyDataSetChanged();
                }
                cursor.close();
            }
        });
        thread.start();
        try {
            thread.join();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

//    /**
//     * Gets the image paths.
//     *
//     * @return Array list of image path strings.
//     */
//    public ArrayList<String> getImagePaths() {
//        return this.mImagePaths;
//    }
}
