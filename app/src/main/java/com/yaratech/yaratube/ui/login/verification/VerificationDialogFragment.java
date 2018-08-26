package com.yaratech.yaratube.ui.login.verification;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.jakewharton.rxbinding2.view.RxView;
import com.jakewharton.rxbinding2.widget.RxTextView;
import com.yaratech.yaratube.R;
import com.yaratech.yaratube.data.model.Event;
import com.yaratech.yaratube.data.source.GlobalBus;
import com.yaratech.yaratube.data.source.UserRepository;
import com.yaratech.yaratube.ui.BaseActivity;
import com.yaratech.yaratube.utils.TextUtils;

import org.greenrobot.eventbus.Subscribe;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;

public class VerificationDialogFragment extends DialogFragment implements VerificationContract.View {
    //------------------------------------------------------------------------------------------
    private static final String TAG = "VerificationDialogFragm";
    private static final String KEY_MESSAGE = "KEY_MESSAGE";
    private static final String KEY_MOBILE_PHONE_NUMBER = "KEY_MOBILE_PHONE_NUMBER";


    @BindView(R.id.et_code_number_verification_code_dialog_input)
    EditText verificationCodeEditText;

    @BindView(R.id.btn_verification_code_dialog_submit_code)
    Button verificationCodeSubmitButton;

    @BindView(R.id.btn_verification_code_dialog_corrent_phone_number)
    Button verificationCodeCorrectButton;

    private VerificationContract.Presenter mPresenter;
    private boolean autoReadOtp = false;
    private Unbinder mUnBinder;
    private UserRepository userRepository;
    private CompositeDisposable compositeDisposable;
    //------------------------------------------------------------------------------------------

    public static VerificationDialogFragment newInstance() {

        Bundle args = new Bundle();
        VerificationDialogFragment fragment = new VerificationDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_verification_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mUnBinder = ButterKnife.bind(this, view);
        mPresenter = new VerificationPresenter(userRepository, compositeDisposable);
        mPresenter.attachView(this);
        if (getArguments() != null) {
            getArguments().putString(KEY_MOBILE_PHONE_NUMBER, mPresenter.getUserMobilePhoneNumber());
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (autoReadOtp) {
            Log.d(TAG, "onResume() called : autoReadOtp is allowed");
            SmsReceiver.bindListener(new SmsListener() {
                @Override
                public void onReceivedMessage(String message) {
                    if (getArguments() != null) {
                        getArguments().putString(KEY_MESSAGE, message);
                        String OTP = TextUtils.removeNonDigits(getArguments().getString(KEY_MESSAGE));
                        Log.d(TAG, "onReceivedMessage(): removeNonDigits Returned : " + OTP);
                        mPresenter.observeAutoReadVerificationCode(getArguments().getString(KEY_MOBILE_PHONE_NUMBER), OTP);
                    }
                }
            });
        } else {
            Log.d(TAG, "onResume() called : autoReadOtp is not allowed");
            Observable observable = RxTextView.textChangeEvents(verificationCodeEditText);
            RxView
                    .clicks(verificationCodeSubmitButton)
                    .first("")
                    .subscribe(new Consumer<Object>() {
                        @Override
                        public void accept(Object o) throws Exception {
                            mPresenter.observeVerificationCodeInput(observable, getArguments().getString(KEY_MOBILE_PHONE_NUMBER));
                        }
                    });

            RxView
                    .clicks(verificationCodeCorrectButton)
                    .first("")
                    .subscribe(new Consumer<Object>() {
                        @Override
                        public void accept(Object o) throws Exception {
                            showLoginStepTwoDialog();
                        }
                    });
        }
    }

    private void showLoginStepTwoDialog() {
        dismiss();
        sendMessageToParentFragment(new Event.ChildParentMessage(Event.MOBILE_PHONE_NUMBER_CORRECT_BUTTON_CLICK_MESSAGE, Event.LOGIN_STEP_TWO));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == BaseActivity.PERMISSION_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                autoReadOtp = true;
            } else {
                autoReadOtp = false;
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroyView() {
        mUnBinder.unbind();
        mPresenter.detachView();
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        Log.d("onDestroy()", "VerificationDialogFragment: ");
        super.onDestroy();
        SmsReceiver.unBindListener();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void closeDialog() {
        dismiss();
    }

    @Subscribe
    public void getMessageFromParentFragment(Event.ParentChildMessage event) {

    }

    public void sendMessageToParentFragment(Event.ChildParentMessage event) {
        GlobalBus.getINSTANCE().post(event);
    }

    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void setCompositeDisposable(CompositeDisposable compositeDisposable) {
        this.compositeDisposable = compositeDisposable;
    }
}
