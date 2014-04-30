/*
   Copyright [2013] [Abhinava Srivastava]

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

//Clase encargada de controlar el fragmento del scannerQR, para modificar el comportamiento ante el resultado ir a Bar
//BarCodeFragment.java, al metodo handleDecode

package com.abhi.barcode.fragment;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Window;
import android.widget.TextView;

import com.scampus.especial1.R;
import com.scampus.tools.dontShowAgain;

import com.scampus.especial1.R;

public class QrCode extends FragmentActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);//LE SACA EL TITULO A LA APP
		setContentView(R.layout.scanner_layout);
		

		
	}
}
