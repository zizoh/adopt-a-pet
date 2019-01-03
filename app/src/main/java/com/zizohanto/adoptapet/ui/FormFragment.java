package com.zizohanto.adoptapet.ui;

import android.app.DatePickerDialog;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ContextThemeWrapper;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
    private HashMap<String, String> mElementIdAndUserInputMap;
    private LinearLayout mBaseLayout;
    private FragmentFormBinding mFragmentFormBinding;
    private OnPageChangeListener mOnPageChangeListener;

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

        Button nextButton = mFragmentFormBinding.nextButton;
        nextButton.setOnClickListener(this);

        Button previousButton = mFragmentFormBinding.previousButton;
        previousButton.setOnClickListener(this);

        if (mCurrentPagePosition == Constants.CURRENT_PAGE_POSITION_FIRST) {
            previousButton.setVisibility(View.GONE);
        } else if (mCurrentPagePosition == Constants.CURRENT_PAGE_POSITION_LAST) {
            nextButton.setText(mContext.getResources().getString(R.string.submit_button));
        }
        return root;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putInt(KEY_COUNT, mCount);
        outState.putStringArrayList(KEY_ACTION_DEPENDENT_VIEWS_UNIQUE_IDS, mActionDependentViewsUniqueIds);
        setElementIdAndUserInputMap();
        outState.putSerializable(KEY_ELEMENT_ID_AND_USER_INPUT_MAP, mElementIdAndUserInputMap);

        super.onSaveInstanceState(outState);
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
                } else if (view1 instanceof LinearLayout) {
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
                                .placeholder(mContext.getResources().getDrawable(R.drawable.no_image))
                                .into(imageView);
                        mBaseLayout.addView(imageView);
                        break;

                    case "text":
                        EditText inputEditText = getEditText();
                        inputEditText.setHint(element.getLabel());
                        setViewTag(element, inputEditText);
                        if (element.getLabel().equals("Email address")) {
                            inputEditText.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                        } else {
                            inputEditText.setInputType(InputType.TYPE_CLASS_TEXT);
                        }
                        if (isADependentElement(element)) {
                            inputEditText.setVisibility(View.GONE);
                        }
                        mBaseLayout.addView(inputEditText);
                        break;

                    case "formattednumeric":
                        EditText numericEditText = getEditText();
                        numericEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
                        numericEditText.setHint(element.getLabel());
                        setViewTag(element, numericEditText);
                        int maxLength = 13;
                        numericEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLength)});
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
                        mBaseLayout.addView(numericEditText);
                        break;

                    case "datetime":
                        LinearLayout linearLayout = new LinearLayout(mContext);
                        LinearLayout.LayoutParams lpDate = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        linearLayout.setLayoutParams(lpDate);
                        linearLayout.setOrientation(LinearLayout.VERTICAL);
                        setViewTag(element, linearLayout);

                        TextView textViewDateLabel = new TextView(mContext);
                        textViewDateLabel.setLayoutParams(lpDate);
                        textViewDateLabel.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                        textViewDateLabel.setTypeface(textViewDateLabel.getTypeface(), Typeface.BOLD);
                        textViewDateLabel.setTextAppearance(android.R.style.TextAppearance_Medium);
                        textViewDateLabel.setText(element.getLabel());
                        setViewTag(element, textViewDateLabel);

                        TextView textViewDate = new TextView(mContext);
                        textViewDate.setLayoutParams(lpDate);
                        textViewDate.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                        textViewDate.setTypeface(textViewDate.getTypeface(), Typeface.BOLD);
                        textViewDate.setInputType(InputType.TYPE_CLASS_DATETIME);
                        textViewDate.setTextAppearance(android.R.style.TextAppearance_Medium);
                        textViewDate.setText(Constants.PICK_A_DATE);
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

    private void restoreViews() {
        int baseLayoutChildViewsCount = mBaseLayout.getChildCount();
        for (int i = 0; i < baseLayoutChildViewsCount; i++) {
            View view1 = mBaseLayout.getChildAt(i);
            if (view1.getVisibility() == View.VISIBLE) {
                if (view1 instanceof EditText) {
                    EditText editText = ((EditText) view1);
                    ArrayList<String> elementIdAndMandatoryTag = (ArrayList<String>) view1.getTag();
                    String elementId = elementIdAndMandatoryTag.get(0);
                    String input = mElementIdAndUserInputMap.get(elementId);
                    editText.setText(input);
                } else if (view1 instanceof Spinner) {
                    ArrayList<String> elementIdAndMandatoryTag = (ArrayList<String>) view1.getTag();
                    String elementId = elementIdAndMandatoryTag.get(0);
                    String input = mElementIdAndUserInputMap.get(elementId);
                    Spinner spinner = (Spinner) view1;
                    int selection;
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

    private ViewGroup.LayoutParams getLayoutParams() {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, 5, 0, 5);
        return layoutParams;
    }

    @NonNull
    private EditText getEditText() {
        EditText inputEditText = new EditText(mContext);
        inputEditText.setLayoutParams(getLayoutParams());
        inputEditText.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
        return inputEditText;
    }

    @NonNull
    private TextView getSectionLabelTextView() {
        TextView textViewLabel = new TextView(mContext);
        LinearLayout.LayoutParams textViewParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        textViewLabel.setLayoutParams(textViewParams);
        textViewLabel.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
        textViewLabel.setTypeface(textViewLabel.getTypeface(), Typeface.BOLD);
        textViewLabel.setTextAppearance(android.R.style.TextAppearance_Medium);
        textViewLabel.setMaxLines(1);
        return textViewLabel;
    }

    private TextView getYesNoLabel() {
        TextView textView = new TextView(mContext);
        LinearLayout.LayoutParams lpYesNo = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        textView.setLayoutParams(lpYesNo);
        textView.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
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
    }

    public boolean isValidated() {
        boolean isValid = true;
        int baseLayoutChildViewsCount = mBaseLayout.getChildCount();
        for (int i = 0; i < baseLayoutChildViewsCount; i++) {
            View view1 = mBaseLayout.getChildAt(i);
            if (view1.getVisibility() == View.VISIBLE) {
                if (view1 instanceof EditText) {
                    EditText editText = ((EditText) view1);
                    ArrayList<String> elementIdAndMandatoryTag = (ArrayList<String>) view1.getTag();
                    String elementId = elementIdAndMandatoryTag.get(0);
                    Boolean elementIsMandatory = Boolean.valueOf(elementIdAndMandatoryTag.get(1));
                    String userInput = editText.getText().toString();
                    if (elementIsMandatory) {
                        if (!userInput.isEmpty()) {
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
        return InputType.TYPE_CLASS_PHONE == editText.getInputType();
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
            if (SharedPreferenceUtils.prefExists("page_" + String.valueOf(i), mContext)) {
                String page = SharedPreferenceUtils.getPrefDefaults("page_" + String.valueOf(i), mContext);
//                JSONObject obj;
//                try {
//                    obj = new JSONObject(page);
//
//                } catch (Throwable t) {
//                    Log.e(mContext.getResources().getString(R.string.app_name),
//                            "Could not parse malformed JSON: \"" + page + "\"");
//                }
                pages.add("Page " + String.valueOf(i) + ": " + page);
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

    interface OnPageChangeListener {

        void onPrevPageClick();

        void onNextPageClick();
    }

}