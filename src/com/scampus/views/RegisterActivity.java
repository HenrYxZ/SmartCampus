package com.scampus.views;

import android.app.Activity;

import java.util.Calendar;
import java.util.List;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.scampus.uc.R;
import com.scampus.tools.RequestHandler;
import com.scampus.tools.User;
import com.scampus.uc.*;
import com.scampus.views.loginActivity.UserLoginTask;

public class RegisterActivity extends Activity{
	
	static final int DATE_DIALOG_ID = 999;
	static final int SELECT_PICTURE = 1;
	
	EditText mEmailView;
	EditText mPasswordView;
	EditText mFirstNameView;
	EditText mLastNameView;
	DatePicker mDatePickerView;
	RadioGroup mRadioGroupView;
	TextView mImageNameView;
	
	String mEmail;
	String mPassword;
	String mFirstName;
	String mLastName;
	Calendar mBirthday;
	String mSex;
	
	private int year;
	private int month;
	private int day;
	
	private RequestQueue requestQueue;
	private RequestHandler requestHandler;
	private User current_user;
	private Uri mProfilePhotoUri;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);
		
		requestQueue = Volley.newRequestQueue(this);
		requestHandler = new RequestHandler(requestQueue);
		current_user = new User(this);
		
		mEmailView = (EditText) findViewById(R.id.email);
		mPasswordView = (EditText) findViewById(R.id.password);
		mFirstNameView = (EditText) findViewById(R.id.firstName);
		mLastNameView = (EditText) findViewById(R.id.lastName);
		mDatePickerView = (DatePicker) findViewById(R.id.datePicker);
		mRadioGroupView = (RadioGroup) findViewById(R.id.radioGroupGender);
		mImageNameView = (TextView) findViewById(R.id.promptImageName);
		
		Button sendButton = (Button) findViewById(R.id.buttonSend);
		Button cancelButton = (Button) findViewById(R.id.buttonCancel);
		Button imageButton = (Button) findViewById(R.id.buttonImage);
		
		mImageNameView.setText("");
		
		final Calendar today = Calendar.getInstance();
		year = today.get(Calendar.YEAR)-20;
		month = today.get(Calendar.MONTH);
		day = today.get(Calendar.DAY_OF_MONTH);
		mDatePickerView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				showDialog(DATE_DIALOG_ID);
				
			}
		});
		
		
		// Segun el ejemplo de 
		// http://www.mkyong.com/android/android-date-picker-example/
		mDatePickerView.init(year, month, day, null);
		
		imageButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// Se debe buscar una imagen de la gallería
                Intent intent = new Intent(Intent.ACTION_PICK, 
                		android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, SELECT_PICTURE);
			}
		});
		
		sendButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				attemptRegister();
			}
		});
		
		cancelButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// Se devuelve a login
				current_user.cleanUser(RegisterActivity.this);
				startActivity(new Intent(v.getContext(), loginActivity.class));
				finish();
			}
		});
		
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
            	List<String> path = data.getData().getPathSegments();
            	mImageNameView.setText(path.get(path.size()-1));
            	current_user = new User(this);
            	this.mProfilePhotoUri = data.getData();
            	Log.i("IMG URI", data.getDataString());
            }
        }
    }
	
	/**
	 * Attempts to register the account specified by the form.
	 * If there are form errors (invalid email, missing fields, etc.), the
	 * errors are presented and no actual login attempt is made.
	 */
	private void attemptRegister() {
		
		// Saca la informacion de todos los campos
		getInfo();
		
		// ¿ Donde obtengo el api token?
		//Reset errors
		mEmailView.setError(null);
		mPasswordView.setError(null);
		mFirstNameView.setError(null);
		mLastNameView.setError(null);
		
		if(validateFields())
		{
			// Setea la información de todos los campos en el objeto de current_user
			getInfo();
			setUser();
			// Envia un POST a la API con la info del current_user
			requestHandler.sendRegisterAsync(this, current_user, this.mProfilePhotoUri);
			startActivity(new Intent(this, loginActivity.class));
			finish();			
		}
		
		
	}
	
	private boolean validateFields() {
		
		boolean cancel = false;
		View focusView = null;

		// Check for a valid password.
		if (TextUtils.isEmpty(mPassword)) {
			mPasswordView.setError(getString(R.string.error_field_required));
			focusView = mPasswordView;
			cancel = true;
		}
		
		if(TextUtils.isEmpty(mFirstName)) {
			mFirstNameView.setError(getString(R.string.error_field_required));
			focusView = mFirstNameView;
			cancel = true;
		}
		
		if(TextUtils.isEmpty(mLastName)) {
			mLastNameView.setError(getString(R.string.error_field_required));
			focusView = mLastNameView;
			cancel = true;
		}

		// Check for a valid email address.
		if (TextUtils.isEmpty(mEmail)) {
			mEmailView.setError(getString(R.string.error_field_required));
			focusView = mEmailView;
			cancel = true;
		} else if (!mEmail.contains("@")) {
			mEmailView.setError(getString(R.string.error_invalid_email));
			focusView = mEmailView;
			cancel = true;
		}
		
		if (mProfilePhotoUri == null) {
			Toast.makeText(this, "Debes adjuntar una imagen de perfil",
					Toast.LENGTH_SHORT).show();
			focusView = (Button) findViewById(R.id.buttonImage);
			cancel = true;
		}
		
		if (cancel) {
			// There was an error; don't attempt register and focus the first
			// form field with an error.
			focusView.requestFocus();
			return false;
		}
		return true;
		
	}
	
	private void setUser() {
		current_user.setEmail(mEmail);
		current_user.setFirstName(mFirstName);
		current_user.setLastName(mLastName);
		current_user.setPassword(mPassword);
		if (mSex.equals("Hombre"))
			current_user.setSex("male");
		else
			current_user.setSex("female");
		current_user.setBirthday(mBirthday);
		current_user.setProvider("native");
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DATE_DIALOG_ID:
		   // set date picker as current date
		   return new DatePickerDialog(this, datePickerListener, 
                         year, month,day);
		}
		return null;
	}
	
	
 
	private DatePickerDialog.OnDateSetListener datePickerListener 
                = new DatePickerDialog.OnDateSetListener() {
 
		// when dialog box is closed, below method will be called.
		public void onDateSet(DatePicker view, int selectedYear,
				int selectedMonth, int selectedDay) {
			year = selectedYear;
			month = selectedMonth;
			day = selectedDay;
 
			// set selected date into datepicker also
			mDatePickerView.init(year, month, day, null);
		}
	};
	
	private void getInfo() {

		mEmail = mEmailView.getText().toString();
		mPassword = mPasswordView.getText().toString();
		mFirstName = mFirstNameView.getText().toString();
		mLastName = mLastNameView.getText().toString();
		int selectedId = mRadioGroupView.getCheckedRadioButtonId();
		mSex = ((RadioButton) findViewById(selectedId)).getText().toString();
		mBirthday = Calendar.getInstance();
		int day = mDatePickerView.getDayOfMonth();
		int month = mDatePickerView.getMonth();
		int year = mDatePickerView.getYear();
		mBirthday.set(year, month, day);
	}
	
}
