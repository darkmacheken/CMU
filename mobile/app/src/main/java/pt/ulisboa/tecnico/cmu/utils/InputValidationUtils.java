package pt.ulisboa.tecnico.cmu.utils;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import pt.ulisboa.tecnico.cmu.R;

public final class InputValidationUtils {

    private InputValidationUtils() {

    }

    /**
     * Performs password validation and returns the view to be focused
     *
     * @param focusView     The current focused view
     * @param password      The password string
     * @param mPasswordView The password view
     * @param context       The current app context
     * @return New view to be focused
     */
    public static View validatePassword(View focusView, String password, EditText mPasswordView, Context context) {
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(context.getString((R.string.error_field_required)));
            return mPasswordView;
        } else if (!isPasswordValid(password)) {
            mPasswordView.setError(context.getString(R.string.error_invalid_password));
            return mPasswordView;
        }
        return focusView;
    }

    /**
     * Performs email validation and returns the view to be focused
     *
     * @param focusView  The current focused view
     * @param email      The email string
     * @param mEmailView The email view
     * @param context    The current app context
     * @return New view to be focused
     */
    public static View validateEmail(View focusView, String email, AutoCompleteTextView mEmailView, Context context) {

        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(context.getString(R.string.error_field_required));
            return mEmailView;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(context.getString(R.string.error_invalid_email));
            return mEmailView;
        }
        return focusView;
    }

    /**
     * Performs confirm-password validation and returns the view to be focused
     *
     * @param focusView            The current focused view
     * @param password             The password string
     * @param confirmPassword      The confirm-password string
     * @param mConfirmPasswordView The confirm-password view
     * @param context              The current app context
     * @return New view to be focused
     */
    public static View validateConfirmPassword(View focusView, String password, String confirmPassword, EditText
        mConfirmPasswordView, Context context) {

        if (TextUtils.isEmpty(confirmPassword)) {
            mConfirmPasswordView.setError(context.getString(R.string.error_field_required));
            return mConfirmPasswordView;
        } else if (!confirmPassword.equals(password)) {
            mConfirmPasswordView.setError(context.getString(R.string.error_passwords_dont_match));
            return mConfirmPasswordView;
        }
        return focusView;
    }

    private static boolean isEmailValid(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private static boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

}
