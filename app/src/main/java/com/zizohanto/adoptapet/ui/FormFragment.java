package com.zizohanto.adoptapet.ui;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ContextThemeWrapper;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;
import com.zizohanto.adoptapet.Constants;
import com.zizohanto.adoptapet.R;
import com.zizohanto.adoptapet.data.Element;
import com.zizohanto.adoptapet.data.FormState;
import com.zizohanto.adoptapet.data.Page;
import com.zizohanto.adoptapet.data.Rule;
import com.zizohanto.adoptapet.data.Section;
import com.zizohanto.adoptapet.databinding.FragmentFormBinding;
import com.zizohanto.adoptapet.utils.SharedPreferenceUtils;
import com.zizohanto.adoptapet.utils.ValidatorUtils;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

@SuppressWarnings("unchecked")
public class FormFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = FormFragment.class.getSimpleName();
    private static final String EXTRA_PAGE = "com.zizohanto.adoptapet.ui.EXTRA_PAGE";
    private static final String EXTRA_PAGE_NUMBER = "com.zizohanto.adoptapet.ui.EXTRA_PAGE_NUMBER";
    private static final String EXTRA_PAGE_POSITION = "com.zizohanto.adoptapet.ui.EXTRA_PAGE_POSITION";

    private static final String KEY_COUNT = "com.zizohanto.adoptapet.ui.EXTRA_COUNT";
    private static final String KEY_ACTION_DEPENDENT_VIEWS_UNIQUE_IDS =
            "com.zizohanto.adoptapet.ui.KEY_ACTION_DEPENDENT_VIEWS_UNIQUE_IDS";
    private static final String KEY_ELEMENT_ID_AND_USER_INPUT_MAP =
            "com.zizohanto.adoptapet.ui.KEY_ELEMENT_ID_AND_USER_INPUT_MAP";


    private Context mContext;
    private ArrayList<String> mActionDependentViewsUniqueIds;
    private Page mPage;
    private int mCurrentPageNumber;
    private int mCurrentPagePosition;
    private int mCount;
    private boolean mIsFirstTimeFragmentCreation = true;
    private HashMap<String, String> mElementIdAndUserInputMap;
    private LinearLayout mBaseLayout;
    private FragmentFormBinding mFragmentFormBinding;
    private OnPageChangeListener mOnPageChangeListener;
    private OnFragmentActivityCreatedListener mOnFragmentActivityCreatedListener;

    public FormFragment() {
        // Requires empty public constructor
    }

    public static FormFragment newInstance(Page page, int pageNumber, int pagePosition) {
        FormFragment fragment = new FormFragment();
        Bundle b = new Bundle();
        Gson gson = new Gson();
        b.putString(EXTRA_PAGE, gson.toJson(page));
        b.putInt(EXTRA_PAGE_NUMBER, pageNumber);
        b.putInt(EXTRA_PAGE_POSITION, pagePosition);
        fragment.setArguments(b);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Gson gson = new Gson();
        if (getArguments() != null) {
            Type type = new TypeToken<Page>() {

            }.getType();

            mPage = gson.fromJson(getArguments().getString(EXTRA_PAGE), type);
            mCurrentPageNumber = getArguments().getInt(EXTRA_PAGE_NUMBER);
            mCurrentPagePosition = getArguments().getInt(EXTRA_PAGE_POSITION);
        }
        mElementIdAndUserInputMap = new HashMap<>();
        mActionDependentViewsUniqueIds = new ArrayList<>();

        if (savedInstanceState != null) {
            mCount = savedInstanceState.getInt(KEY_COUNT);
            mActionDependentViewsUniqueIds = savedInstanceState.getStringArrayList(KEY_ACTION_DEPENDENT_VIEWS_UNIQUE_IDS);
            mElementIdAndUserInputMap = (HashMap<String, String>) savedInstanceState.getSerializable(KEY_ELEMENT_ID_AND_USER_INPUT_MAP);
        }
    }

    @SuppressLint("RestrictedApi")
    @Override
    public View onCreateView(@Nullable LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mFragmentFormBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_form, container, false);
        View root = mFragmentFormBinding.getRoot();

        mContext = getActivity();
        mBaseLayout = mFragmentFormBinding.baseLayout;

        displayForm();

        if (savedInstanceState != null) {
            restoreViews();
        }

        FloatingActionButton nextButton = mFragmentFormBinding.nextButton;
        nextButton.setOnClickListener(this);

        FloatingActionButton previousButton = mFragmentFormBinding.previousButton;
        previousButton.setOnClickListener(this);

        if (mCurrentPagePosition == Constants.CURRENT_PAGE_POSITION_FIRST) {
            previousButton.setVisibility(View.GONE);
        } else if (mCurrentPagePosition == Constants.CURRENT_PAGE_POSITION_LAST) {
            Log.e(TAG, "CURRENT_PAGE_POSITION_LAST: ");
            nextButton.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_done_24dp));
        }
        return root;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putInt(KEY_COUNT, mCount);
        outState.putStringArrayList(KEY_ACTION_DEPENDENT_VIEWS_UNIQUE_IDS, mActionDependentViewsUniqueIds);
        outState.putSerializable(KEY_ELEMENT_ID_AND_USER_INPUT_MAP, mElementIdAndUserInputMap);

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mOnFragmentActivityCreatedListener.onFragmentActivityCreated(mIsFirstTimeFragmentCreation);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        setElementIdAndUserInputMap();
        mIsFirstTimeFragmentCreation = false;
    }

    private void setElementIdAndUserInputMap() {
        int baseLayoutChildViewsCount = mBaseLayout.getChildCount();
        for (int i = 0; i < baseLayoutChildViewsCount; i++) {
            View view1 = mBaseLayout.getChildAt(i);
            if (view1.getVisibility() == View.VISIBLE) {
                if (view1 instanceof EditText) {
                    EditText editText = ((EditText) view1);
                    ArrayList<String> elementIdAndMandatoryTag = (ArrayList<String>) view1.getTag();
                    String elementId = elementIdAndMandatoryTag.get(0);
                    String userInput = editText.getText().toString();
                    mElementIdAndUserInputMap.put(elementId, userInput);

                } else if (view1 instanceof Spinner) {
                    ArrayList<String> elementIdAndMandatoryTag = (ArrayList<String>) view1.getTag();
                    String elementId = elementIdAndMandatoryTag.get(0);
                    String text = ((Spinner) view1).getSelectedItem().toString();
                    mElementIdAndUserInputMap.put(elementId, text);
                } else if (view1 instanceof LinearLayout && !(view1 instanceof TextInputLayout)) {
                    LinearLayout ll = ((LinearLayout) view1);
                    TextView dateTextView = (TextView) ll.getChildAt(1);
                    ArrayList<String> elementIdAndMandatoryTag = (ArrayList<String>) dateTextView.getTag();
                    String elementId = elementIdAndMandatoryTag.get(0);
                    String userInput = dateTextView.getText().toString();
                    mElementIdAndUserInputMap.put(elementId, userInput);
                }
            }
        }
    }

    private void displayForm() {
        List<Section> sections = mPage.getSections();
        for (int i = 0; i < sections.size(); i++) {
            Section section = sections.get(i);
            TextView textViewLabel = getSectionLabelTextView();
            textViewLabel.setText(section.getLabel());
            mBaseLayout.addView(textViewLabel);

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
                        ImageView imageView = new ImageView(mContext);
                        LinearLayout.LayoutParams lpEmbeddedPhoto = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 600);
                        imageView.setLayoutParams(lpEmbeddedPhoto);
                        imageView.setAdjustViewBounds(true);
                        imageView.setContentDescription("Photo");
                        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);

                        Picasso.with(mContext)
                                .load(element.getFile())
                                .placeholder(ContextCompat.getDrawable(mContext, R.drawable.no_image))
                                .into(imageView);
                        mBaseLayout.addView(imageView);
                        break;

                    case "text":
                        TextInputLayout textInputLayout = getTextInputLayout();
                        textInputLayout.setHint(element.getLabel());
                        if (element.getIsMandatory()) {
                            textInputLayout.setHelperText("*Required now");
                        }
                        TextInputEditText inputEditText = getTextInputEditText(textInputLayout);
                        View.generateViewId();
                        int textId = getNumberFromTextElementId(element.getUniqueId());
                        inputEditText.setId(textId);
                        textInputLayout.addView(inputEditText);
                        setViewTag(element, textInputLayout);
                        setViewTag(element, inputEditText);

                        setViewTag(element, inputEditText);
                        if (element.getLabel().equals("Email address")) {
                            inputEditText.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                        } else {
                            inputEditText.setInputType(InputType.TYPE_CLASS_TEXT);
                        }
                        if (isADependentElement(element)) {
                            textInputLayout.setVisibility(View.GONE);
                        }
                        mBaseLayout.addView(textInputLayout);
                        break;

                    case "formattednumeric":
                        TextInputLayout numericInputLayout = getTextInputLayout();
                        numericInputLayout.setHint(element.getLabel());
                        if (element.getIsMandatory()) {
                            numericInputLayout.setHelperText("*Required now");
                        }
                        TextInputEditText numericEditText = getTextInputEditText(numericInputLayout);
                        numericInputLayout.addView(numericEditText);
                        setViewTag(element, numericInputLayout);
                        setViewTag(element, numericEditText);

                        if (isADependentElement(element)) {
                            numericEditText.setVisibility(View.GONE);
                        }

                        View.generateViewId();
                        int numericId = getNumberFromNumericElementId(element.getUniqueId());
                        // To differentiate the ids of formattednumeric and text
                        numericEditText.setId(numericId + 100);
                        numericEditText.setInputType(InputType.TYPE_CLASS_NUMBER);

                        numericEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(13)});
                        numericEditText.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                            }

                            @Override
                            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                            }

                            @Override
                            public void afterTextChanged(Editable editable) {
                                int inputLength = numericEditText.getText().toString().length();
                                if (mCount <= inputLength && (inputLength == 4 || inputLength == 8)) {
                                    numericEditText.setText(String.format("%s-", numericEditText.getText().toString()));
                                    int pos = numericEditText.getText().length();
                                    numericEditText.setSelection(pos);
                                } else if (mCount >= inputLength && (inputLength == 4 || inputLength == 8)) {
                                    numericEditText.setText(numericEditText.getText().toString().substring(0, inputLength - 1));
                                    int pos = numericEditText.getText().length();
                                    numericEditText.setSelection(pos);
                                }
                                mCount = inputLength;

                            }
                        });
                        if (isADependentElement(element)) {
                            numericEditText.setVisibility(View.GONE);
                        }
                        mBaseLayout.addView(numericInputLayout);
                        break;

                    case "datetime":
                        LinearLayout linearLayout = new LinearLayout(mContext);
                        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                        linearLayout.setOrientation(LinearLayout.VERTICAL);
                        setViewTag(element, linearLayout);

                        TextView textViewDateLabel = new TextView(mContext);
                        LinearLayout.LayoutParams lpDate = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        lpDate.setMargins(8, 0, 0, 0);
                        textViewDateLabel.setLayoutParams(lpDate);
                        textViewDateLabel.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimaryDark));

                        textViewDateLabel.setTypeface(textViewDateLabel.getTypeface(), Typeface.BOLD);
                        textViewDateLabel.setTextAppearance(android.R.style.TextAppearance_Medium);
                        textViewDateLabel.setText(element.getLabel());
                        setViewTag(element, textViewDateLabel);

                        TextView textViewDate = new TextView(mContext);
                        textViewDate.setLayoutParams(lpDate);
                        textViewDate.setTextColor(ContextCompat.getColor(mContext, R.color.white));
                        textViewDate.setTypeface(textViewDate.getTypeface(), Typeface.BOLD);
                        textViewDate.setInputType(InputType.TYPE_CLASS_DATETIME);
                        textViewDate.setTextAppearance(android.R.style.TextAppearance_Medium);
                        textViewDate.setText(Constants.PICK_A_DATE);
                        textViewDate.setBackgroundColor(ContextCompat.getColor(mContext, R.color.boxcolor));
                        textViewDate.setPadding(8, 8, 8, 8);
                        setViewTag(element, textViewDate);

                        textViewDate.setOnClickListener(view -> {
                            Calendar calendar = Calendar.getInstance();
                            int year = calendar.get(Calendar.YEAR);
                            int month = calendar.get(Calendar.MONTH);
                            int day = calendar.get(Calendar.DAY_OF_MONTH);
                            DatePickerDialog startDateDialog = new DatePickerDialog(mContext, (datePicker, year1, month1, day1) -> {
                                String selDate = year1 + "-" + (month1 + 1) + "-" + day1;
                                textViewDate.setText(getFormattedDate(selDate));
                                textViewDate.setError(null);
                            }, year, month, day);
                            calendar.setTime(new Date());
                            calendar.add(Calendar.YEAR, 0);
                            startDateDialog.getDatePicker().setMaxDate(calendar.getTimeInMillis());
                            startDateDialog.show();
                        });
                        linearLayout.addView(textViewDateLabel);
                        linearLayout.addView(textViewDate);
                        if (isADependentElement(element)) {
                            linearLayout.setVisibility(View.GONE);
                        }
                        mBaseLayout.addView(linearLayout);
                        break;

                    case "yesno":
                        TextView textViewYesNo = getYesNoLabel();
                        textViewYesNo.setText(element.getLabel());

                        Spinner inputSpinner = new Spinner(mContext);
                        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        layoutParams.setMargins(0, 5, 0, 5);
                        inputSpinner.setLayoutParams(layoutParams);
                        // populate spinner with String Array
                        ArrayAdapter<String> adapter = populateSpinner(mContext);
                        inputSpinner.setAdapter(adapter);
                        setViewTag(element, inputSpinner);
                        if (isADependentElement(element)) {
                            textViewYesNo.setVisibility(View.GONE);
                            inputSpinner.setVisibility(View.GONE);
                        }
                        mBaseLayout.addView(textViewYesNo);
                        mBaseLayout.addView(inputSpinner);

                        inputSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                                List<Rule> rules = element.getRules();
                                if (rules != null) {
                                    for (Rule rule : rules) {
                                        List<String> targets = rule.getTargets();
                                        for (String target : targets) {
                                            if (inputSpinner.getSelectedItem().toString().equals("Yes")) {
                                                showViewWithTag(target, View.VISIBLE);
                                            } else showViewWithTag(target, View.GONE);
                                        }
                                    }
                                }
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> adapterView) {

                            }
                        });
                        break;
                }
            }
        }
    }

    private int getNumberFromTextElementId(String uniqueId) {
        return Integer.valueOf(uniqueId.substring(5));
    }

    private int getNumberFromNumericElementId(String uniqueId) {
        return Integer.valueOf(uniqueId.substring(17));
    }

    private void showViewWithTag(String target, int visible) {
        int baseLayoutChildViewsCount = mBaseLayout.getChildCount();
        for (int j = 0; j < baseLayoutChildViewsCount; j++) {
            View view1 = mBaseLayout.getChildAt(j);
            Object tag = view1.getTag();
            if (tag != null) {
                ArrayList<String> elementIdAndMandatoryTag = (ArrayList<String>) tag;
                String elementId = elementIdAndMandatoryTag.get(0);
                if (elementId.equals(target)) {
                    view1.setVisibility(visible);
                }
            }
        }
    }

    private TextInputLayout getTextInputLayout() {
        TextInputLayout textInputLayout = new TextInputLayout(new ContextThemeWrapper(mContext,
                R.style.myStyle));
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, 8, 0, 0);
        textInputLayout.setLayoutParams(layoutParams);
        textInputLayout.setBoxBackgroundColor(
                ContextCompat.getColor(mContext, R.color.boxcolor));
        textInputLayout.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);
        textInputLayout.setPadding(18, 0, 0, 0);
        return textInputLayout;
    }

    private TextInputEditText getTextInputEditText(TextInputLayout textInputLayout) {
        TextInputEditText editText = new TextInputEditText(textInputLayout.getContext());
        editText.setInputType(InputType.TYPE_CLASS_TEXT);
        editText.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        return editText;
    }

    @NonNull
    private TextView getSectionLabelTextView() {
        TextView textViewLabel = new TextView(mContext);
        LinearLayout.LayoutParams textViewParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        textViewParams.setMargins(0, 16, 0, 8);
        textViewLabel.setLayoutParams(textViewParams);
        textViewLabel.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimaryDark));

        textViewLabel.setTypeface(textViewLabel.getTypeface(), Typeface.BOLD);
        textViewLabel.setTextAppearance(android.R.style.TextAppearance_Medium);
        textViewLabel.setMaxLines(1);
        return textViewLabel;
    }

    private TextView getYesNoLabel() {
        TextView textView = new TextView(mContext);
        LinearLayout.LayoutParams lpYesNo = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        textView.setLayoutParams(lpYesNo);
        textView.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimaryDark));
        textView.setTypeface(textView.getTypeface(), Typeface.BOLD);
        textView.setTextAppearance(android.R.style.TextAppearance_Medium);
        return textView;
    }

    private boolean isADependentElement(Element element) {
        return mActionDependentViewsUniqueIds.contains(element.getUniqueId());
    }

    private void setViewTag(Element element, View view) {
        ArrayList<String> elementIdAndMandatoryTag = new ArrayList<>();
        elementIdAndMandatoryTag.add(element.getUniqueId());
        elementIdAndMandatoryTag.add(String.valueOf(element.getIsMandatory()));
        view.setTag(elementIdAndMandatoryTag);
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
        if (context instanceof OnFragmentActivityCreatedListener) {
            mOnFragmentActivityCreatedListener = (OnFragmentActivityCreatedListener) context;
        } else {
            throw new ClassCastException(context.toString()
                    + getString(R.string.error_interface));
        }
    }

    public boolean isValidated() {
        boolean isValid = true;
        int baseLayoutChildViewsCount = mBaseLayout.getChildCount();
        for (int i = 0; i < baseLayoutChildViewsCount; i++) {
            View view1 = mBaseLayout.getChildAt(i);
            if (view1.getVisibility() == View.VISIBLE) {
                if (view1 instanceof TextInputLayout) {
                    ArrayList<String> elementIdAndMandatoryTag = (ArrayList<String>) view1.getTag();
                    String elementId = elementIdAndMandatoryTag.get(0);
                    int textId;
                    try {
                        textId = getNumberFromTextElementId(elementId);
                    } catch (NumberFormatException e) {
                        textId = getNumberFromNumericElementId(elementId) + 100;
                    }
                    View view2 = view1.findViewById(textId);
                    if (view2 instanceof EditText) {
                        EditText editText = ((EditText) view2);
                        Boolean elementIsMandatory = Boolean.valueOf(elementIdAndMandatoryTag.get(1));
                        String userInput = editText.getText().toString();
                        if (elementIsMandatory) {
                            if (!userInput.isEmpty()) {
                                Log.e(TAG, "isValidated: " + userInput);
                                if (isEmailEntry(editText)) {
                                    if (!ValidatorUtils.isValidEmailAddress(userInput)) {
                                        isValid = false;
                                        editText.setError("Please enter a valid email");
                                    } else mElementIdAndUserInputMap.put(elementId, userInput);
                                }
                                if (isPhoneEntry(editText)) {
                                    if (!ValidatorUtils.isValidPhoneNumber(userInput)) {
                                        isValid = false;
                                        editText.setError("Please enter a valid Nigerian phone number");
                                    } else mElementIdAndUserInputMap.put(elementId, userInput);
                                }
                                mElementIdAndUserInputMap.put(elementId, userInput);
                            } else {
                                isValid = false;
                                editText.setError("Please enter the required text");
                            }
                        } else mElementIdAndUserInputMap.put(elementId, userInput);
                    }
                } else if (view1 instanceof Spinner) {
                    ArrayList<String> elementIdAndMandatoryTag = (ArrayList<String>) view1.getTag();
                    String elementId = elementIdAndMandatoryTag.get(0);
                    Boolean elementIsMandatory = Boolean.valueOf(elementIdAndMandatoryTag.get(1));
                    String text = ((Spinner) view1).getSelectedItem().toString();
                    if (elementIsMandatory) {
                        if (!text.equals("Select")) {
                            mElementIdAndUserInputMap.put(elementId, text);
                        } else {
                            isValid = false;
                            Toast.makeText(mContext, "Please select an entry", Toast.LENGTH_SHORT).show();
                        }
                    } else mElementIdAndUserInputMap.put(elementId, text);

                } else if (view1 instanceof LinearLayout) {
                    LinearLayout ll = ((LinearLayout) view1);
                    TextView dateTextView = (TextView) ll.getChildAt(1);
                    ArrayList<String> elementIdAndMandatoryTag = (ArrayList<String>) dateTextView.getTag();
                    String elementId = elementIdAndMandatoryTag.get(0);
                    Boolean elementIsMandatory = Boolean.valueOf(elementIdAndMandatoryTag.get(1));
                    String userInput = dateTextView.getText().toString();
                    if (elementIsMandatory) {
                        if (userInput.equals(Constants.PICK_A_DATE)) {
                            isValid = false;
                            dateTextView.setError("Please pick a date");
                        } else {
                            mElementIdAndUserInputMap.put(elementId, userInput);
                        }
                    } else mElementIdAndUserInputMap.put(elementId, userInput);
                }
            }
        }
        return isValid;
    }

    private boolean isEmailEntry(EditText editText) {
        return InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS == editText.getInputType();
    }

    private boolean isPhoneEntry(EditText editText) {
        return InputType.TYPE_CLASS_NUMBER == editText.getInputType();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.next_button:
                if (isValidated()) {
                    saveValue();
                    if (mCurrentPagePosition == Constants.CURRENT_PAGE_POSITION_LAST) {
                        showVerifyInputsDialog();
                    } else mOnPageChangeListener.onNextPageClick();
                }
                break;
            case R.id.previous_button:
                mOnPageChangeListener.onPrevPageClick();
                break;
        }
    }

    private void saveValue() {
        String jsonString = SharedPreferenceUtils.convertToJSONString(mElementIdAndUserInputMap);
        SharedPreferenceUtils.setPrefDefaults("page_" + String.valueOf(mCurrentPageNumber), jsonString, mContext);
    }

    private void showVerifyInputsDialog() {
        ArrayList<String> pages = new ArrayList<>();
        for (int i = 0; i < mCurrentPageNumber + 1; i++) {
            try {
                String page = SharedPreferenceUtils.getPrefDefaults("page_" + String.valueOf(i), mContext);
                pages.add("Page " + String.valueOf(i) + ": " + page);
            } catch (NullPointerException e) {
                System.out.print("NullPointerException caught: " + e);
            }
        }
        String message = TextUtils.join("\n", pages);

        WebView webView = new WebView(mContext);
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(),
                R.style.AlertDialogCustom));
        builder.setTitle("Inputs")
                .setView(webView)
                .setCancelable(true)
                .setMessage(message)
                .show();
    }

    public FormState getFormState() {
        return new FormState(mElementIdAndUserInputMap);
    }

    public void restoreState(FormState state) {
        mElementIdAndUserInputMap = state.getElementIdAndUserInputMap();
        restoreViews();
    }

    private void restoreViews() {
        int baseLayoutChildViewsCount = mBaseLayout.getChildCount();
        for (int i = 0; i < baseLayoutChildViewsCount; i++) {
            View view1 = mBaseLayout.getChildAt(i);
            if (view1.getVisibility() == View.VISIBLE) {
                if (view1 instanceof TextInputLayout) {
                    ArrayList<String> elementIdAndMandatoryTag = (ArrayList<String>) view1.getTag();
                    String elementId = elementIdAndMandatoryTag.get(0);
                    int textId;
                    try {
                        textId = getNumberFromTextElementId(elementId);
                    } catch (NumberFormatException e) {
                        textId = getNumberFromNumericElementId(elementId + 100);
                    }
                    View view2 = view1.findViewById(textId);
                    if (view2 instanceof EditText) {
                        EditText editText = ((EditText) view2);
                        String input = mElementIdAndUserInputMap.get(elementId);
                        editText.setText(input);
                    }
                } else if (view1 instanceof Spinner) {
                    ArrayList<String> elementIdAndMandatoryTag = (ArrayList<String>) view1.getTag();
                    String elementId = elementIdAndMandatoryTag.get(0);
                    String input = mElementIdAndUserInputMap.get(elementId);
                    Spinner spinner = (Spinner) view1;
                    int selection = 0;
                    if (input != null) {
                        switch (input) {
                            case "Select":
                                selection = 0;
                                break;
                            case "Yes":
                                selection = 1;
                                break;
                            default:
                                selection = 2;
                                break;
                        }
                    }
                    spinner.setSelection(selection);
                } else if (view1 instanceof LinearLayout) {
                    LinearLayout ll = ((LinearLayout) view1);
                    TextView dateTextView = (TextView) ll.getChildAt(1);
                    ArrayList<String> elementIdAndMandatoryTag = (ArrayList<String>) dateTextView.getTag();
                    String elementId = elementIdAndMandatoryTag.get(0);
                    String input = mElementIdAndUserInputMap.get(elementId);
                    dateTextView.setText(input);
                }
            }
        }
    }

    interface OnPageChangeListener {

        void onPrevPageClick();

        void onNextPageClick();
    }

    interface OnFragmentActivityCreatedListener {

        void onFragmentActivityCreated(Boolean isFirstTimeCreation);
    }

}