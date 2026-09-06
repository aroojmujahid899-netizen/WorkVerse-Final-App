package com.workverse.app.activities.employee;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.workverse.app.R;
import com.workverse.app.models.Feedback;
import com.workverse.app.utils.AIFeedbackAnalyzer;
import com.workverse.app.utils.FirebaseHelper;
import com.workverse.app.utils.SharedPrefManager;
import java.util.ArrayList;
import java.util.List;
public class SubmitFeedbackActivity extends AppCompatActivity {
    Button btnSubmit; ProgressBar pb;
    @Override protected void onCreate(Bundle s){
        super.onCreate(s);
        setContentView(R.layout.activity_submit_feedback);
        Toolbar tb=findViewById(R.id.toolbar);setSupportActionBar(tb);tb.setNavigationOnClickListener(v->finish());
        btnSubmit=findViewById(R.id.btnSubmit);pb=findViewById(R.id.progressBar);
        List<TextInputEditText> eds=new ArrayList<>();
        findEditTexts((android.view.ViewGroup)((android.view.ViewGroup)findViewById(android.R.id.content)).getChildAt(0),eds);
        btnSubmit.setOnClickListener(v->{
            if(eds.size()<2){Toast.makeText(this,"Fill all fields",Toast.LENGTH_SHORT).show();return;}
            String title=eds.get(0).getText().toString().trim();
            String msg=eds.get(1).getText().toString().trim();
            if(TextUtils.isEmpty(title)||TextUtils.isEmpty(msg)){
                Toast.makeText(this,"All fields required",Toast.LENGTH_SHORT).show();return;}
            pb.setVisibility(View.VISIBLE);btnSubmit.setEnabled(false);
            SharedPrefManager spm=SharedPrefManager.getInstance(this);
            Feedback fb=new Feedback(spm.getUid(),spm.getFullName()!=null?spm.getFullName():"Employee",title,msg,"Employee");
            // Analyze sentiment from the feedback text and save it so it displays correctly everywhere
            String sentiment = AIFeedbackAnalyzer.analyzeSentiment(msg + " " + title);
            fb.setSentiment(sentiment);
            FirebaseHelper.getDb().collection(FirebaseHelper.COL_FEEDBACK).add(fb)
                    .addOnSuccessListener(r->{pb.setVisibility(View.GONE);
                        Toast.makeText(this,"Feedback submitted!",Toast.LENGTH_SHORT).show();finish();})
                    .addOnFailureListener(e->{pb.setVisibility(View.GONE);btnSubmit.setEnabled(true);
                        Toast.makeText(this,"Failed",Toast.LENGTH_SHORT).show();});
        });
    }
    private void findEditTexts(android.view.ViewGroup vg,List<TextInputEditText> list){
        for(int i=0;i<vg.getChildCount();i++){View c=vg.getChildAt(i);
            if(c instanceof TextInputEditText)list.add((TextInputEditText)c);
            else if(c instanceof android.view.ViewGroup)findEditTexts((android.view.ViewGroup)c,list);}
    }
}
