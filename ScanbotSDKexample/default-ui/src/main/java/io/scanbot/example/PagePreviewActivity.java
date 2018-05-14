package io.scanbot.example;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import net.doo.snap.camera.CameraPreviewMode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.scanbot.sdk.ScanbotSDK;
import io.scanbot.sdk.persistance.Page;
import io.scanbot.sdk.persistance.PageFileStorage;
import io.scanbot.sdk.ui.view.base.configuration.CameraOrientationMode;
import io.scanbot.sdk.ui.view.camera.CameraActivity;
import io.scanbot.sdk.ui.view.camera.configuration.CameraConfiguration;
import io.scanbot.sdk.ui.view.edit.EditPolygonActivity;
import io.scanbot.sdk.ui.view.edit.configuration.EditPolygonConfiguration;

public class PagePreviewActivity extends AppCompatActivity {

    private int CAMERA_DEFAULT_UI_REQUEST_CODE = 1111;
    private int CROP_DEFAULT_UI_REQUEST_CODE = 9999;

    private PagesAdapter adapter;
    private RecyclerView recycleView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page_preview);

        adapter = new PagesAdapter();
        adapter.setHasStableIds(true);

        recycleView = findViewById(R.id.pages_preview);
        recycleView.setHasFixedSize(true);
        recycleView.setAdapter(adapter);

        GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        recycleView.setLayoutManager(layoutManager);

        if (isRelaunchedAfterStateRestore()) {
            return;
        } else {
            CameraConfiguration cameraConfiguration = new CameraConfiguration();

            cameraConfiguration.setCameraPreviewMode(CameraPreviewMode.FIT_IN);
            cameraConfiguration.setOrientationMode(CameraOrientationMode.PORTRAIT);
            cameraConfiguration.setIgnoreBadAspectRatio(true);
            cameraConfiguration.setAutoSnappingEnabled(false);

            Intent intent = io.scanbot.sdk.ui.view.camera.CameraActivity.newIntent(this, cameraConfiguration);
            startActivityForResult(intent, CAMERA_DEFAULT_UI_REQUEST_CODE);
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_DEFAULT_UI_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Parcelable[] parcelableArrayExtra = data.getParcelableArrayExtra(CameraActivity.SNAPPED_PAGE_EXTRA);
            List<Page> pages = new ArrayList<>();
            for (Parcelable parcelable : parcelableArrayExtra) {
                pages.add((Page) parcelable);
            }
            adapter.setItems(pages);
        } else if (requestCode == CROP_DEFAULT_UI_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Page page = data.getParcelableExtra(EditPolygonActivity.EDITED_PAGE_EXTRA);
            adapter.updateItem(page);
        }
    }


    private boolean isRelaunchedAfterStateRestore() {
        return getLastNonConfigurationInstance() != null;
    }

    class PagesAdapter extends RecyclerView.Adapter<PageViewHolder> {

        List<Page> items = new ArrayList<>();
        private View.OnClickListener mOnClickListener = new MyOnClickListener();

        void setItems(List<Page> pages) {
            items.clear();
            items.addAll(pages);
            notifyDataSetChanged();
        }

        @Override
        public PageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_page, parent, false);
            view.setOnClickListener(mOnClickListener);
            return new PageViewHolder(view);
        }

        @Override
        public void onBindViewHolder(PageViewHolder holder, int position) {
            Page page = items.get(position);
            Picasso.with(getApplicationContext())
                    .load(new File(new ScanbotSDK(PagePreviewActivity.this).getPageFileStorage().getPreviewImageURI(page.getPageId(), PageFileStorage.PageFileType.DOCUMENT)))
                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                    .resizeDimen(R.dimen.move_preview_size, R.dimen.move_preview_size)
                    .centerInside()
                    .into(holder.imageView);
        }

        @Override
        public long getItemId(int position) {
            return items.get(position).hashCode();
        }

        @Override
        public int getItemCount() {
            return items.size();
        }


        public void updateItem(Page page) {
            List<Page> renewedItems = new ArrayList();
            for (Page item : items) {
                if (!item.getPageId().equals(page.getPageId())) {
                    renewedItems.add(item);
                }
            }
            renewedItems.add(page);
            setItems(renewedItems);
        }
    }

    class MyOnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            int itemPosition = recycleView.getChildLayoutPosition(v);
            Page item = adapter.items.get(itemPosition);
            EditPolygonConfiguration editPolygonConfiguration = new EditPolygonConfiguration();

            editPolygonConfiguration.setPage(
                    item
            );

            Intent intent = io.scanbot.sdk.ui.view.edit.EditPolygonActivity.newIntent(
                    getApplicationContext(),
                    editPolygonConfiguration
            );
            startActivityForResult(intent, CROP_DEFAULT_UI_REQUEST_CODE);
        }
    }

    /**
     * View holder for page and its number.
     */
    class PageViewHolder extends RecyclerView.ViewHolder {

        private final ImageView imageView;

        public PageViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.page);
        }

    }

}
