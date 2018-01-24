package com.freakz.matukaa.examprep;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.freakz.matukaa.examprep.domain.MyEntity;


public class DetailFragment extends Fragment {
    public static final String ARG_ITEM_ID = "item_id";

    private MyEntity mItem;

    public DetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null && getArguments().containsKey(ARG_ITEM_ID)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            mItem = new MyEntity(Integer.parseInt(getArguments().getString(ARG_ITEM_ID))); // TODO: implement correct constructor

            Activity activity = this.getActivity();
            assert activity != null;
            CollapsingToolbarLayout appBarLayout = activity.findViewById(R.id.toolbar_layout);
            if (appBarLayout != null) {
                //appBarLayout.setTitle(mItem.getTitle()); TODO fix this
            }
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        // Show the dummy content as text in a TextView.
        if (mItem != null) {
            ((TextView) rootView.findViewById(R.id.detail_fragment)).setText("asdf"); // TODO: mItem.getTitle()
        }

        return rootView;
    }
}
