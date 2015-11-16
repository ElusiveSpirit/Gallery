package adv.android_11.solleks.homework2;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by Константин on 15.11.2015.
 *
 *  Диалоговый фрагмент для выбора названия нового альбома
 */
public class NewAlbumFragment extends DialogFragment implements View.OnClickListener {

    private EditText mTextEdit;
    private OnAlertFragmentListener alertFragmentListener;

    private void succeed(String name) {
        alertFragmentListener.newAlbum(name);
        // TODO Добавить проверку на корректность навзания
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            alertFragmentListener = (OnAlertFragmentListener) context;
        }catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " должен реализовывать интерфейс OnFragmentInteractionListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().setTitle(R.string.message_text);
        View view = inflater.inflate(R.layout.fragment_dialog, container, false);
        mTextEdit = (EditText) view.findViewById(R.id.dialogEditText);
        Button buttonYes = (Button) view.findViewById(R.id.dialog_yes);
        Button buttonNo = (Button) view.findViewById(R.id.dialog_no);

        buttonNo.setOnClickListener(this);
        buttonYes.setOnClickListener(this);



        return view;
    }

    public String getText() {
        if (mTextEdit != null)
            return mTextEdit.getText().toString();
        return null;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.dialog_no :
                mTextEdit = null;
                NewAlbumFragment.this.dismiss();
                break;
            case R.id.dialog_yes :
                succeed(getText());
                NewAlbumFragment.this.dismiss();
                break;
        }
    }

    public interface OnAlertFragmentListener {
        void newAlbum(String albumName);
    }
}
