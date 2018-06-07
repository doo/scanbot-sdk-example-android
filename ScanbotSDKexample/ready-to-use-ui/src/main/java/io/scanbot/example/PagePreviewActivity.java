package io.scanbot.example;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
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

import net.doo.snap.lib.detector.DetectionResult;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.scanbot.sdk.ScanbotSDK;
import io.scanbot.sdk.persistence.Page;
import io.scanbot.sdk.persistence.PageFileStorage;
import io.scanbot.sdk.ui.view.edit.CroppingActivity;
import io.scanbot.sdk.ui.view.edit.configuration.CroppingConfiguration;

public class PagePreviewActivity extends AppCompatActivity {

    private int CROP_UI_REQUEST_CODE = 9999;

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

        List<Page> pages = new ArrayList<>();
        try {
            List<String> storedPages = new ScanbotSDK(this).getPageFileStorage().getStoredPages();

            for (String storedPage : storedPages) {
                pages.add(new Page(storedPage, new ArrayList<>(), DetectionResult.OK));
            }

            adapter.setItems(pages);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CROP_UI_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Page page = data.getParcelableExtra(CroppingActivity.EDITED_PAGE_EXTRA);
            adapter.updateItem(page);
        }
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

            String imagePath = new ScanbotSDK(getApplication()).getPageFileStorage().getPreviewImageURI(page.getPageId(), PageFileStorage.PageFileType.DOCUMENT).getPath();
            String originalImagePath = new ScanbotSDK(getApplication()).getPageFileStorage().getPreviewImageURI(page.getPageId(), PageFileStorage.PageFileType.ORIGINAL).getPath();
            File fileToShow = (new File(imagePath).exists()) ? new File(imagePath) : new File(originalImagePath);

            Picasso.with(getApplicationContext())
                    .load(fileToShow)
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
            Page page = adapter.items.get(itemPosition);
            CroppingConfiguration configuration = new CroppingConfiguration();
            // Customize colors, text resources, etc via configuration:
            //configuration.set...

            configuration.setPage(page);

            Intent intent = CroppingActivity.newIntent(getApplicationContext(), configuration);
            startActivityForResult(intent, CROP_UI_REQUEST_CODE);
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
