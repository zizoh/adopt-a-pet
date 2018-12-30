package com.zizohanto.adoptapet.ui;

import android.app.DatePickerDialog;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;
import com.zizohanto.adoptapet.R;
import com.zizohanto.adoptapet.data.Element;
import com.zizohanto.adoptapet.data.Page;
import com.zizohanto.adoptapet.data.Rule;
import com.zizohanto.adoptapet.data.Section;
import com.zizohanto.adoptapet.databinding.FragmentFormBinding;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FormFragment extends Fragment implements View.OnClickListener {

    private static final String EXTRA_PAGE = "com.zizohanto.adoptapet.ui.EXTRA_PAGE";
    static Gson mGson;
    private ArrayList<String> mActionDependentViewsUniqueIds = new ArrayList<>();
    private Page mPage;
    private FragmentFormBinding mFragmentFormBinding;
    private OnPageChangeListener mOnPageChangeListener;

    public FormFragment() {
        // Requires empty public constructor
    }

    public static FormFragment newInstance(Page page) {
        FormFragment fragment = new FormFragment();
        Bundle b = new Bundle();
        mGson = new Gson();
        b.putString(EXTRA_PAGE, mGson.toJson(page));
        fragment.setArguments(b);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            Type type = new TypeToken<Page>() {

            }.getType();
            mPage = mGson.fromJson(getArguments().getString(EXTRA_PAGE), type);
        }
    }

    @Override
    public View onCreateView(@Nullable LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mFragmentFormBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_form, container, false);
        View root = mFragmentFormBinding.getRoot();

        Context context = getActivity();
        List<Section> sections = mPage.getSections();
        for (int i = 0; i < sections.size(); i++) {
            Section section = sections.get(i);
            TextView textViewLabel = new TextView(context);
            LinearLayout.LayoutParams textViewParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            textViewLabel.setLayoutParams(textViewParams);
            textViewLabel.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
            textViewLabel.setTypeface(textViewLabel.getTypeface(), Typeface.BOLD);
            textViewLabel.setTextAppearance(android.R.style.TextAppearance_Medium);
            textViewLabel.setMaxLines(1);
            textViewLabel.setText(section.getLabel());
            LinearLayout baseLayout = mFragmentFormBinding.baseLayout;
            baseLayout.addView(textViewLabel);

            List<Element> elements = section.getElements();
            for (Element element : elements) {
                if (element.getRules() != null) {
                    List<Rule> rules = element.getRules();
                    for (Rule rule : rules) {
                        List<String> targets = rule.getTargets();
                        mActionDependentViewsUniqueIds.addAll(targets);
                    }
                }
                String type = element.getType();
                switch (type) {
                    case "embeddedphoto":
                        if (!mActionDependentViewsUniqueIds.contains(element.getUniqueId())) {
                            ImageView imageView = new ImageView(context);
                            LinearLayout.LayoutParams lpEmbeddedPhoto = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            imageView.setLayoutParams(lpEmbeddedPhoto);
                            imageView.setAdjustViewBounds(true);
                            imageView.setContentDescription("Pet Photo");
                            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);

                            Picasso.with(context)
                                    .load(element.getFile())
                                    .placeholder(context.getResources().getDrawable(R.drawable.no_image))
                                    .into(imageView);
                            baseLayout.addView(imageView);
                        }
                        break;

                    case "text":
                        if (!mActionDependentViewsUniqueIds.contains(element.getUniqueId())) {
                            EditText inputEditText = new EditText(context);
                            LinearLayout.LayoutParams lpText = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            lpText.setMargins(0, 5, 0, 5);
                            inputEditText.setLayoutParams(lpText);
                            // set textColor
                            inputEditText.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                            inputEditText.setHint(element.getLabel());
                            if (element.getLabel().equals("Email address")) {
                                inputEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                            }
                            inputEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
                            // add the EditText(s) to the parent layout
                            baseLayout.addView(inputEditText);
                        }
                        break;

                    case "formattednumeric":
                        if (!mActionDependentViewsUniqueIds.contains(element.getUniqueId())) {
                            EditText numericEditText = new EditText(context);
                            LinearLayout.LayoutParams lpNumericText = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            lpNumericText.setMargins(0, 5, 0, 5);
                            numericEditText.setLayoutParams(lpNumericText);
                            numericEditText.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                            numericEditText.setInputType(InputType.TYPE_CLASS_PHONE);
                            numericEditText.setHint(element.getLabel());
                            // add the EditText(s) to the parent layout
                            baseLayout.addView(numericEditText);
                        }
                        break;

                    case "datetime":
                        if (mActionDependentViewsUniqueIds.contains(element.getUniqueId())) {
                            TextView textViewDate = new TextView(context);
                            LinearLayout.LayoutParams lpDate = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            textViewDate.setLayoutParams(lpDate);
                            textViewDate.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                            textViewDate.setTypeface(textViewDate.getTypeface(), Typeface.BOLD);
                            textViewDate.setTextAppearance(android.R.style.TextAppearance_Medium);
                            textViewDate.setText(element.getLabel());
                            textViewDate.setOnClickListener(view -> {
                                Calendar calendar = Calendar.getInstance();
                                int year = calendar.get(Calendar.YEAR);
                                int month = calendar.get(Calendar.MONTH);
                                int day = calendar.get(Calendar.DAY_OF_MONTH);
                                DatePickerDialog startDateDialog = new DatePickerDialog(context, (datePicker, year1, month1, day1) -> {
                                    String selDate = year1 + "-" + (month1 + 1) + "-" + day1;
                                    textViewDate.setText(getFormattedDate(selDate));
                                }, year, month, day);
                                calendar.setTime(new Date());
                                calendar.add(Calendar.YEAR, 0);
                                startDateDialog.getDatePicker().setMaxDate(calendar.getTimeInMillis());
                                startDateDialog.show();
                            });
                            baseLayout.addView(textViewDate);
                        }
                        break;

                    case "yesno":
                        if (!mActionDependentViewsUniqueIds.contains(element.getUniqueId())) {
                            TextView textViewYesNo = new TextView(context);
                            LinearLayout.LayoutParams lpYesNo = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            textViewYesNo.setLayoutParams(lpYesNo);
                            textViewYesNo.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                            textViewYesNo.setTypeface(textViewYesNo.getTypeface(), Typeface.BOLD);
                            textViewYesNo.setTextAppearance(android.R.style.TextAppearance_Medium);
                            textViewYesNo.setText(element.getLabel());
                            baseLayout.addView(textViewYesNo);

                            Spinner inputSpinner = new Spinner(context);
                            lpYesNo.setMargins(0, 5, 0, 5);
                            inputSpinner.setLayoutParams(lpYesNo);
                            // populate spinner with String Array
                            ArrayAdapter<String> adapter = populateSpinner(context);
                            inputSpinner.setAdapter(adapter);
                            // add the spinner(s) to the parent layout
                            baseLayout.addView(inputSpinner);

                            EditText inputEditText = new EditText(context);
                            LinearLayout.LayoutParams lpText = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            lpText.setMargins(0, 5, 0, 5);
                            inputEditText.setLayoutParams(lpText);
                            // set textColor
                            inputEditText.setTextColor(FormFragment.this.getResources().getColor(R.color.colorPrimaryDark));

                            inputEditText.setHint(element.getLabel());
                            baseLayout.addView(inputEditText);
                            inputEditText.setVisibility(View.GONE);
                            inputSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                                    if (inputSpinner.getSelectedItem().toString().equals("Yes")) {
                                        inputEditText.setVisibility(View.VISIBLE);
                                    } else inputEditText.setVisibility(View.GONE);
                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> adapterView) {

                                }
                            });
                        }
                        break;
                }
            }
        }
        Button mNextBtn = mFragmentFormBinding.nextButton;
        mNextBtn.setOnClickListener(this);
        Button mPrevBtn = mFragmentFormBinding.previousButton;
        mPrevBtn.setOnClickListener(this);
        return root;
    }

    @NonNull
    private ArrayAdapter<String> populateSpinner(Context context) {
        String[] arraySpinner = new String[]{"Select", "Yes", "No"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                android.R.layout.simple_spinner_item, arraySpinner);
        adapter.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);
        return adapter;
    }

    private String getFormattedDate(String selectedDate) {

        SimpleDateFormat monthDate = new SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        Date date = null;
        try {
            date = sdf.parse(selectedDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return monthDate.format(date);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnPageChangeListener) {
            mOnPageChangeListener = (OnPageChangeListener) context;
        } else {
            throw new ClassCastException(context.toString()
                    + getString(R.string.error_interface));
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.next_button:
                mOnPageChangeListener.onNextPageClick();
                break;
            case R.id.previous_button:
                mOnPageChangeListener.onPrevPageClick();
                break;
        }
    }

    public interface OnPageChangeListener {

        void onPrevPageClick();

        void onNextPageClick();
    }

}
