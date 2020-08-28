package com.quintonvr.tjoptjop.activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.text.StringSearch;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;

import com.quintonvr.tjoptjop.BuildConfig;
import com.quintonvr.tjoptjop.R;
import com.quintonvr.tjoptjop.helpers.DatabaseHelper;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.quintonvr.tjoptjop.helpers.Utils.INTENT_EXTRA_ADDRESS;
import static com.quintonvr.tjoptjop.helpers.Utils.INTENT_EXTRA_CITY;
import static com.quintonvr.tjoptjop.helpers.Utils.INTENT_EXTRA_DIRECTION;
import static com.quintonvr.tjoptjop.helpers.Utils.INTENT_EXTRA_EMERGENCY_CONTACT;
import static com.quintonvr.tjoptjop.helpers.Utils.INTENT_EXTRA_IDTYPE;
import static com.quintonvr.tjoptjop.helpers.Utils.INTENT_EXTRA_ID_NUMBER;
import static com.quintonvr.tjoptjop.helpers.Utils.INTENT_EXTRA_MASK;
import static com.quintonvr.tjoptjop.helpers.Utils.INTENT_EXTRA_NAME;
import static com.quintonvr.tjoptjop.helpers.Utils.INTENT_EXTRA_TEMPERATURE;
import static com.quintonvr.tjoptjop.helpers.Utils.INTENT_EXTRA_TEMPTYPE;
import static com.quintonvr.tjoptjop.helpers.Utils.INTENT_EXTRA_DIRECTION;
import static com.quintonvr.tjoptjop.helpers.Utils.INTENT_EXTRA_WELLNESS;
import static com.quintonvr.tjoptjop.helpers.Utils.PREFS_ACTIVATION_TOKEN;
import static com.quintonvr.tjoptjop.helpers.Utils.PREFS_CUSTOMER_CODE;
import static com.quintonvr.tjoptjop.helpers.Utils.PREFS_QUESTIONS;
import static com.quintonvr.tjoptjop.helpers.Utils.PREFS_QUESTIONS_ID;
import static com.quintonvr.tjoptjop.helpers.Utils.VITAL_PREFS;

public class QuestionsActivity extends AppCompatActivity{
    private static final String TAG = "QuizM";
    private static final String OPTION_SEP = ", ";
    private static final char SPLIT = ';';
    private static final String QUESTION_FIELD_SPLIT = ";";
    private static final int font_size = 18;

    private ArrayList<String> resultSet = new ArrayList<>();
    private LinearLayout questionspace;
    private DatabaseHelper mydb;
    private String q0 = "";
    private String q1 = "";
    private String q2 = "";
    private String q3 = "";
    private String q4 = "";
    private String q5 = "";
    private String q6 = "";
    private String q7 = "";
    private String q8 = "";
    private String q9 = "";
    private String q10 = "";
    private String q11 = "";
    private String q12 = "";
    private String q13 = "";
    private String q14 = "";
    private String q15 = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questions);
        questionspace = (LinearLayout)findViewById(R.id.questionspace);
        QuestionSplitter();
        Button submitBtn = findViewById(R.id.submitBtn);

        Intent intent = getIntent();
        if (intent != null) {
            SharedPreferences prefs = getSharedPreferences(VITAL_PREFS, Context.MODE_PRIVATE);
            String customerCode = prefs.getString(PREFS_CUSTOMER_CODE, "");
            String activationToken = prefs.getString(PREFS_ACTIVATION_TOKEN, "");
            int questionID = prefs.getInt(PREFS_QUESTIONS_ID,-1);
            String direction = intent.getStringExtra(INTENT_EXTRA_DIRECTION);
            String userID = intent.getStringExtra(INTENT_EXTRA_ID_NUMBER);
            String temperature = intent.getStringExtra(INTENT_EXTRA_TEMPERATURE);
            String tempMeta = intent.getStringExtra(INTENT_EXTRA_TEMPTYPE);
            String idType = intent.getStringExtra(INTENT_EXTRA_IDTYPE);
            String RecordMeta = "{COM;"+ BuildConfig.VERSION_CODE+";"+idType+";QID="+Integer.toString(questionID)+"}";
            String mask = intent.getStringExtra(INTENT_EXTRA_MASK);
            if (intent.getStringExtra(INTENT_EXTRA_NAME) != null) {
                q12 = intent.getStringExtra(INTENT_EXTRA_NAME);
                q13 = intent.getStringExtra(INTENT_EXTRA_EMERGENCY_CONTACT);
                q14 = intent.getStringExtra(INTENT_EXTRA_ADDRESS);
                q15 = intent.getStringExtra(INTENT_EXTRA_CITY);
            }
            q0 = intent.getStringExtra(INTENT_EXTRA_WELLNESS);

            mydb = new DatabaseHelper(this);
            submitBtn.setOnClickListener(v -> {
                String questionSlots[] = {"q2","q3","q4","q5","q6","q7","q8","q9","q10","q11"};
                Map<String,String> questionMap = new HashMap<String,String>();
                for (int i=0;i<resultSet.size();i++){
                    questionMap.put(questionSlots[i],"");
                }

                if (resultSet.size()>10){
                    /*Error cannot transport results*/
                }else{
                    for (int i=0;i<resultSet.size();i++){
                        questionMap.put(questionSlots[i],resultSet.get(i));
                    }
                }

                if (mydb.insertNewValue(customerCode, activationToken, userID, temperature, tempMeta, q0, mask,
                        questionMap.get(questionSlots[0]), questionMap.get(questionSlots[1]), questionMap.get(questionSlots[2]),
                        questionMap.get(questionSlots[3]), questionMap.get(questionSlots[4]), questionMap.get(questionSlots[5]),
                        questionMap.get(questionSlots[6]), questionMap.get(questionSlots[7]), questionMap.get(questionSlots[8]),
                        questionMap.get(questionSlots[9]),q12,q13,q14,q15,direction,RecordMeta)) {
                    mydb.close();

                    AlertDialog.Builder dialog = new AlertDialog.Builder(this)
                            .setTitle("Successfully Stored")
                            .setMessage("Select OK to continue")
                            .setPositiveButton(R.string.button_ok, ((dialogInterface, i) -> {
                                dialogInterface.dismiss();
                                Intent homeIntent = new Intent(this, MainActivity.class);
                                startActivity(homeIntent);
                                finishAffinity();
                            }))
                            .setCancelable(false);
                    dialog.show();
                } else {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(this)
                            .setTitle("Something went wrong")
                            .setMessage("Please try again.")
                            .setPositiveButton(R.string.button_ok, ((dialogInterface, i) -> {
                                dialogInterface.dismiss();
                            }))
                            .setCancelable(false);
                    dialog.show();
                }
            });

        }

    }

    private void QuestionSplitter(){
        SharedPreferences prefs = getSharedPreferences(VITAL_PREFS, Context.MODE_PRIVATE);
        String questionSet = prefs.getString(PREFS_QUESTIONS,"");
        Matcher regex = Pattern.compile("(?<=\\[)(.*?)(?=\\])").matcher(questionSet);
        int question_index = 0;
        while (regex.find()){
            switch(regex.group(0).toCharArray()[0]){
                case 'W':{
                    Log.i(TAG,"Found Wellness Question - "+regex.group(0));
                    question_index--;
                }break;
                case 'K':{
                    Log.i(TAG,"Found Mask Question - "+regex.group(0));
                    question_index--;
                }break;
                case 'B':{
                    Log.i(TAG,"Found Binary Question - "+regex.group(0));
                    String question = regex.group(0).split(QUESTION_FIELD_SPLIT)[1];
                    String defOption = regex.group(0).split(QUESTION_FIELD_SPLIT)[2];
                    addBinary(questionspace,question,defOption,question_index);
                }break;
                case 'M':{
                    Log.i(TAG,"Found Multiple Choice Question - "+regex.group(0));
                }break;
                case 'T': {
                    Log.i(TAG, "Found Text input Question - " + regex.group(0));
                    String question = regex.group(0).split(QUESTION_FIELD_SPLIT)[1];
                    String defRes = regex.group(0).split(QUESTION_FIELD_SPLIT)[2];
                    String expand = regex.group(0).split(QUESTION_FIELD_SPLIT)[3];
                    addComboText(questionspace,question,defRes,expand,question_index);
                }break;
                case 'C':{
                    Log.i(TAG,"Found Combo Option Question - "+regex.group(0));
                    String question = regex.group(0).split(QUESTION_FIELD_SPLIT)[1];
                    String defRes = regex.group(0).split(QUESTION_FIELD_SPLIT)[2];
                    String expand = regex.group(0).substring(regex.group(0).lastIndexOf('>')+2);
                    String options = "";
                    Matcher regexQ = Pattern.compile("(?<=\\<)(.*?)(?=\\>)").matcher(regex.group(0));
                    if (regexQ.find()){
                        options = regexQ.group(0);
                    }
                    addComboOption(questionspace,question,defRes,options,expand,question_index);
                }break;
            }
            question_index++;
        }
    }

    /* This function adds a question that when answered in the affirmative presents the user with a selection of options to choose from
     * Input: container - View to which the elements should be added
     *        leader - Question that is presented to the user
     *        LeaderDefault - The text value that is returned by default (thus the negative of the question asked
     *        Options - The list of options that should be presented to the user (delimiters removed, split values included
     *        Expand - Default state of the slider. If Y slider is active and options are expanded
     *        resultIndex - Questions are numbered sequentially from 0 through N. Index is used to keep track of user responses.
     */
    private void addComboOption(LinearLayout container, String Leader, final String LeaderDefault, String Options, String Expand, final int resultIndex){
        /* Leader question first*/
        Switch LeaderQuestion = new Switch(this);
        LeaderQuestion.setText(Leader);
        LeaderQuestion.setTextSize(TypedValue.COMPLEX_UNIT_SP,font_size);
        if (resultSet.size() < resultIndex+1){
            resultSet.add("");
        }
        resultSet.set(resultIndex,LeaderDefault);
        container.addView(LeaderQuestion);
        /*Space to put the new option*/
        final LinearLayout subOptions = new LinearLayout(this);
        subOptions.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        subOptions.setOrientation(LinearLayout.VERTICAL);
        subOptions.setVisibility(View.GONE);
        container.addView(subOptions);
        /*Add the individual options*/
        do{
            int split_loc = Options.indexOf(SPLIT);
            if (split_loc < 0 ){ split_loc = Options.length(); }
            final String Qopt = Options.substring(0,split_loc);
            CheckBox Option = new CheckBox(this);
            Option.setText(Qopt);
            Option.setTextSize(TypedValue.COMPLEX_UNIT_SP,font_size);
            subOptions.addView(Option);
            Option.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    //If value is default remove it first
                    if (resultSet.get(resultIndex).contains(LeaderDefault)){
                        resultSet.set(resultIndex,resultSet.get(resultIndex).replace(LeaderDefault,""));
                    }
                    if ((resultSet.get(resultIndex).contains(Qopt)) || (resultSet.get(resultIndex).contains(OPTION_SEP+Qopt))){
                        if (resultSet.get(resultIndex).contains(OPTION_SEP+Qopt)){
                            resultSet.set(resultIndex,resultSet.get(resultIndex).replace(OPTION_SEP+Qopt,""));
                        }else{
                            resultSet.set(resultIndex,resultSet.get(resultIndex).replace(Qopt,""));
                        }
                        if (resultSet.get(resultIndex).isEmpty()){
                            resultSet.set(resultIndex,LeaderDefault);
                        }else{
                            if (resultSet.get(resultIndex).indexOf(OPTION_SEP)==0){
                                resultSet.set(resultIndex,resultSet.get(resultIndex).substring(OPTION_SEP.length()));
                            }
                        }
                    }else{
                        if (resultSet.get(resultIndex).isEmpty()){
                            resultSet.set(resultIndex,Qopt);
                        }else{
                            resultSet.set(resultIndex,resultSet.get(resultIndex)+OPTION_SEP+Qopt);
                        }
                    }
                }
            });
            if (split_loc < Options.length()) { //Split was found in options, so repeat
                Options = Options.substring(split_loc + 1);
            }else{
                Options = "";
            }
        }while (Options.length() > 0);
        /*Show/Hide the options*/
        if (Expand.equals("Y")){
            LeaderQuestion.setChecked(true);
            subOptions.setVisibility(View.VISIBLE);
        }else{
            LeaderQuestion.setChecked(false);
            subOptions.setVisibility(View.GONE);
        }
        LeaderQuestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (subOptions.getVisibility() == View.VISIBLE){
                    subOptions.setVisibility(View.GONE);
                    resultSet.set(resultIndex,LeaderDefault);
                }else{
                    subOptions.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    /* This function adds a question that when answered in the affirmative presents the user with a text entry field.
     * Input: container - View to which the elements should be added
     *        leader - Question that is presented to the user
     *        LeaderDefault - The text value that is returned by default (thus the negative of the question asked
     *        resultIndex - Questions are numbered sequentially from 0 through N. Index is used to keep track of user responses.
     */
    private void addComboText(LinearLayout container, String Leader, final String LeaderDefault, String Expand, final int resultIndex){
        /* Leader question first*/
        Switch LeaderQuestion = new Switch(this);
        LeaderQuestion.setText(Leader);
        LeaderQuestion.setTextSize(TypedValue.COMPLEX_UNIT_SP,font_size);
        if (resultSet.size() < resultIndex+1){
            resultSet.add("");
        }
        resultSet.set(resultIndex,LeaderDefault);
        container.addView(LeaderQuestion);
        /*Space to put the new option*/
        final LinearLayout subOptions = new LinearLayout(this);
        subOptions.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        subOptions.setOrientation(LinearLayout.VERTICAL);
        subOptions.setVisibility(View.GONE);
        container.addView(subOptions);
        /*Add the inputfield*/
        final EditText userInput = new EditText(this);
        userInput.setTextSize(TypedValue.COMPLEX_UNIT_SP,font_size);
        subOptions.addView(userInput);
        userInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                resultSet.set(resultIndex,userInput.getText().toString());
            }
        });
        /*Show/Hide the input field*/
        if (Expand.equals("Y")){
            LeaderQuestion.setChecked(true);
            subOptions.setVisibility(View.VISIBLE);
        }else{
            LeaderQuestion.setChecked(false);
            subOptions.setVisibility(View.GONE);
        }
        LeaderQuestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (subOptions.getVisibility() == View.VISIBLE){
                    subOptions.setVisibility(View.GONE);
                    resultSet.set(resultIndex,LeaderDefault);
                }else{
                    subOptions.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    /* This function adds a question that has a binary nature (thus Yes/No)
     * Input: container - View to which the elements should be added
     *        leader - Question that is presented to the user
     *        LeaderDefault - The default state of the response. Optional. If not provided (""/null) No is defaulted.
     *        resultIndex - Questions are numbered sequentially from 0 through N. Index is used to keep track of user responses.
     */
    private void addBinary(LinearLayout container, String Leader, final String LeaderDefault, final int resultIndex){
        /* Leader question first*/
        final Switch LeaderQuestion = new Switch(this);
        LeaderQuestion.setText(Leader);
        LeaderQuestion.setTextSize(TypedValue.COMPLEX_UNIT_SP,font_size);
        if (resultSet.size() < resultIndex+1){
            resultSet.add("");
        }
        resultSet.set(resultIndex,LeaderDefault);
        if (LeaderDefault.equals("Y")){
            LeaderQuestion.setChecked(true);
        }else{
            LeaderQuestion.setChecked(false);
        }
        container.addView(LeaderQuestion);
        /*Show/Hide the input field*/
        LeaderQuestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (LeaderQuestion.isChecked()){
                    resultSet.set(resultIndex,LeaderDefault);
                }else{
                    resultSet.set(resultIndex,"N");
                }
            }
        });
    }
}
