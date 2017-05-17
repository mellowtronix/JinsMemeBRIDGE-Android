/**
 * SettingFragment.java
 *
 * Copylight (C) 2017, Shunichi Yamamoto(Yamamoto Works Ltd.)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 **/

package com.jins_meme.bridge;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.List;

public class OSCConfigFragment extends Fragment {

  private RelativeLayout layout;
  private EditText etRemoteIP;
  private EditText etRemotePort;
  private EditText etHostIP;
  private EditText etHostPort;
  private Button btnTest;

  private MemeOSC testOSC;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    return inflater.inflate(R.layout.fragment_oscconfig, container, false);
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
  }

  @Override
  public void onDetach() {
    super.onDetach();

    etRemoteIP = null;
    etRemotePort = null;
    etHostIP = null;
    etHostPort = null;
    btnTest = null;

    if(testOSC != null) {
      testOSC.closeSocket();
      testOSC = null;
    }
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    layout = (RelativeLayout) view.findViewById(R.id.osc_layout);
    layout.setOnTouchListener(new OnTouchListener() {
      @Override
      public boolean onTouch(View view, MotionEvent motionEvent) {
        Log.d("DEBUG", "view touch.");

        layout.requestFocus();

        return false;
      }
    });

    ((MainActivity) getActivity()).setActionBarTitle(getString(R.string.osc_conf) + " SETTING");
    ((MainActivity) getActivity()).setActionBarBack(true);

    InputFilter[] filters = new InputFilter[1];
    filters[0] = new InputFilter() {
      @Override
      public CharSequence filter(CharSequence source, int start,
          int end, Spanned dest, int dstart, int dend) {
        if (end > start) {
          String destTxt = dest.toString();
          String resultingTxt = destTxt.substring(0, dstart) +
              source.subSequence(start, end) +
              destTxt.substring(dend);
          if (!resultingTxt.matches ("^\\d{1,3}(\\." +
              "(\\d{1,3}(\\.(\\d{1,3}(\\.(\\d{1,3})?)?)?)?)?)?")) {
            return "";
          } else {
            String[] splits = resultingTxt.split("\\.");
            for (int i=0; i<splits.length; i++) {
              if (Integer.valueOf(splits[i]) > 255) {
                return "";
              }
            }
          }
        }
        return null;
      }
    };

    etRemoteIP = (EditText) view.findViewById(R.id.remote_ip);
    String savedRemoteIP = ((MainActivity) getActivity()).getSavedValue("REMOTE_IP", "255.255.255.255");
    if (savedRemoteIP.equals("255.255.255.255")) {
      etRemoteIP.setText(MemeOSC.getRemoteIPv4Address());
    }
    else {
      etRemoteIP.setText(savedRemoteIP);
    }
    etRemoteIP.setFilters(filters);
    etRemoteIP.setOnFocusChangeListener(new OnFocusChangeListener() {
      @Override
      public void onFocusChange(View view, boolean b) {
        if (!b) {
          InputMethodManager imm = (InputMethodManager)((MainActivity) getActivity()).getSystemService(Context.INPUT_METHOD_SERVICE);
          imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
      }
    });
    etRemoteIP.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

      }

      @Override
      public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

      }

      @Override
      public void afterTextChanged(Editable editable) {
        Log.d("DEBUG", "after text changed " + editable.toString());

        ((MainActivity) getActivity()).autoSaveValue("REMOTE_IP", editable.toString());

        testOSC.setRemoteIP(etRemoteIP.getText().toString());
        testOSC.initSocket();
      }
    });

    etRemotePort = (EditText) view.findViewById(R.id.remote_port);
    etRemotePort.setText(String.valueOf(((MainActivity) getActivity()).getSavedValue("REMOTE_PORT", 10316)));
    etRemotePort.setOnFocusChangeListener(new OnFocusChangeListener() {
      @Override
      public void onFocusChange(View view, boolean b) {
        if (!b) {
          InputMethodManager imm = (InputMethodManager)((MainActivity) getActivity()).getSystemService(Context.INPUT_METHOD_SERVICE);
          imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
      }
    });
    etRemotePort.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

      }

      @Override
      public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

      }

      @Override
      public void afterTextChanged(Editable editable) {
        Log.d("DEBUG", "after text changed " + editable.toString());

        ((MainActivity) getActivity()).autoSaveValue("REMOTE_PORT", Integer.valueOf(editable.toString()));

        testOSC.setRemotePort(Integer.parseInt(etRemotePort.getText().toString()));
        testOSC.initSocket();
      }
    });

    etHostIP = (EditText) view.findViewById(R.id.host_ip);
    etHostIP.setText(MemeOSC.getHostIPv4Address());
    etHostIP.setEnabled(false);

    etHostPort = (EditText) view.findViewById(R.id.host_port);
    etHostPort.setText(String.valueOf(((MainActivity) getActivity()).getSavedValue("HOST_PORT", 11316)));
    etHostPort.setOnFocusChangeListener(new OnFocusChangeListener() {
      @Override
      public void onFocusChange(View view, boolean b) {
        if (!b) {
          InputMethodManager imm = (InputMethodManager)((MainActivity) getActivity()).getSystemService(Context.INPUT_METHOD_SERVICE);
          imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
      }
    });
    etHostPort.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

      }

      @Override
      public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

      }

      @Override
      public void afterTextChanged(Editable editable) {
        Log.d("DEBUG", "after text changed " + editable.toString());

        ((MainActivity) getActivity()).autoSaveValue("HOST_PORT", Integer.valueOf(editable.toString()));

        testOSC.setHostPort(Integer.parseInt(etHostPort.getText().toString()));
        testOSC.initSocket();
      }
    });

    btnTest = (Button) view.findViewById(R.id.remote_test);
    btnTest.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        testOSC.setAddress("/meme/bridge", "/test");
        testOSC.setTypeTag("si");
        testOSC.addArgument(etRemoteIP.getText().toString());
        testOSC.addArgument(Integer.parseInt(etRemotePort.getText().toString()));
        testOSC.flushMessage();
      }
    });

    testOSC = new MemeOSC();
    testOSC.setRemoteIP(etRemoteIP.getText().toString());
    testOSC.setRemotePort(Integer.parseInt(etRemotePort.getText().toString()));
    testOSC.setHostPort(Integer.parseInt(etHostPort.getText().toString()));
    testOSC.initSocket();
  }
}