package app.reasoning.logical.quiz.craftystudio.logicalreasoning;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;
import com.google.firebase.messaging.FirebaseMessaging;

import io.fabric.sdk.android.Fabric;

import java.util.ArrayList;
import java.util.Random;

import utils.AppRater;
import utils.ClickListener;
import utils.FireBaseHandler;
import utils.Questions;
import utils.TopicListAdapter;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    FireBaseHandler fireBaseHandler;

    ArrayList<String> mArraylist;
    ListView topicAndTestListview;
    TopicListAdapter adapter;

    Toolbar toolbar;
    int check;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        fireBaseHandler = new FireBaseHandler();
        openDynamicLink();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });


        FirebaseMessaging.getInstance().subscribeToTopic("subscribed");
        try {
            AppRater.app_launched(MainActivity.this);
        } catch (Exception e) {
            e.printStackTrace();
        }


        BottomNavigationView bottomNavigationView = (BottomNavigationView)
                findViewById(R.id.mainactivity_navigation_menu);

        bottomNavigationView.setOnNavigationItemSelectedListener
                (new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.bottombar_main_topic:
                                downloadTopicList();
                                break;
                            case R.id.bottombar_main_daily:
                                downloadDateList();
                                break;

                        }
                        return true;
                    }
                });

    }

    public void uploadTopicName() {
        FireBaseHandler fireBaseHandler = new FireBaseHandler();
        fireBaseHandler.uploadTopicName("Time and Work", new FireBaseHandler.OnTopiclistener() {
            @Override
            public void onTopicDownLoad(String topic, boolean isSuccessful) {

                if (isSuccessful) {
                    Toast.makeText(MainActivity.this, topic + " Topic Uploaded ", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onTopicListDownLoad(ArrayList<String> topicList, boolean isSuccessful) {

            }

            @Override
            public void onTopicUpload(boolean isSuccessful) {


            }
        });

    }


    public void initializeActivity() {


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mArraylist = new ArrayList<>();
        topicAndTestListview = (ListView) findViewById(R.id.topicActivity_topic_listview);


        //calling rate now dialog
        // AppRater appRater = new AppRater();
        // appRater.app_launched(TopicActivity.this);


        //download list of Topics
        showDialog("Loading...Please wait");
        downloadTopicList();


    }

    private void downloadTopicList() {
        FireBaseHandler fireBaseHandler = new FireBaseHandler();

        fireBaseHandler.downloadTopicList(30, new FireBaseHandler.OnTopiclistener() {
            @Override
            public void onTopicDownLoad(String topic, boolean isSuccessful) {

            }

            @Override
            public void onTopicListDownLoad(ArrayList<String> topicList, boolean isSuccessful) {

                if (isSuccessful) {
                    //  Toast.makeText(TopicActivity.this, "size is " + topicList.size(), Toast.LENGTH_SHORT).show();

                    mArraylist = topicList;
                    adapter = new TopicListAdapter(getApplicationContext(), R.layout.custom_textview, mArraylist);

                    adapter.setOnItemCLickListener(new ClickListener() {
                        @Override
                        public void onItemCLickListener(View view, int position) {
                            TextView textview = (TextView) view;

                            openTopicQuestionActivity(0, textview.getText().toString(), null);
                            //  Toast.makeText(TopicActivity.this, " Selected " + textview.getText().toString(), Toast.LENGTH_SHORT).show();

                        }
                    });

                    topicAndTestListview.post(new Runnable() {
                        public void run() {
                            topicAndTestListview.setAdapter(adapter);
                        }
                    });


                    hideDialog();

                }
                hideDialog();

            }

            @Override
            public void onTopicUpload(boolean isSuccessful) {

            }
        });


    }


    public void downloadDateList() {
        fireBaseHandler.downloadDateList(15, new FireBaseHandler.OnTestSerieslistener() {
            @Override
            public void onTestDownLoad(String test, boolean isSuccessful) {

            }

            @Override
            public void onTestListDownLoad(ArrayList<String> testList, boolean isSuccessful) {
                if (isSuccessful) {

                    mArraylist.clear();

                    for (String name : testList) {
                        mArraylist.add(name);
                    }

                    //check = 1 is Test Series
                    check = 1;
                    adapter = new TopicListAdapter(getApplicationContext(), R.layout.custom_textview, mArraylist);

                    adapter.setOnItemCLickListener(new ClickListener() {
                        @Override
                        public void onItemCLickListener(View view, int position) {
                            TextView textview = (TextView) view;
                            if (check == 1) {

                                openTopicQuestionActivity(1, textview.getText().toString(), null);

                                //  Toast.makeText(TopicActivity.this, "In Test " + " Selected " + textview.getText().toString() + " Postion is " + position, Toast.LENGTH_SHORT).show();

                                try {
                                    Answers.getInstance().logCustom(new CustomEvent("Daily Quiz open").putCustomAttribute("Date Name", textview.getText().toString()));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                            }
                        }
                    });


                    topicAndTestListview.post(new Runnable() {
                        public void run() {
                            topicAndTestListview.setAdapter(adapter);
                        }
                    });


                    hideDialog();

                }
                hideDialog();
            }

            @Override
            public void onTestUpload(boolean isSuccessful) {

            }

        });
    }


    public void openRandomTestActivity(View view) {
        Intent intent = new Intent(MainActivity.this, RandomTestActivity.class);
        startActivity(intent);
    }

    private void openDynamicLink() {

        //dowmload topiclist
        initializeActivity();

        FirebaseDynamicLinks.getInstance()
                .getDynamicLink(getIntent())
                .addOnSuccessListener(this, new OnSuccessListener<PendingDynamicLinkData>() {
                    @Override
                    public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {
                        // Get deep link from result (may be null if no link is found)
                        Uri deepLink = null;
                        if (pendingDynamicLinkData != null) {
                            deepLink = pendingDynamicLinkData.getLink();
                            Log.d("DeepLink", "onSuccess: " + deepLink);

                            String questionID = deepLink.getQueryParameter("questionID");
                            String questionTopicName = deepLink.getQueryParameter("questionTopic");


                            //download question
                            if (questionID != null) {
                                openTopicQuestionActivity(0, questionTopicName, questionID);
                                try {
                                    Answers.getInstance().logCustom(new CustomEvent("Via dyanamic link").putCustomAttribute("question id", questionID));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }


                        } else {
                            Log.d("DeepLink", "onSuccess: ");

                            //download story list


                            try {
                                Intent intent = getIntent();
                                String questionID = intent.getStringExtra("questionID");
                                String questionTopicName = intent.getStringExtra("questionTopic");
                                if (questionID == null) {

                                } else {
                                    //download story
                                    openTopicQuestionActivity(0, questionTopicName, questionID);
                                    try {
                                        Answers.getInstance().logCustom(new CustomEvent("Via push notification").putCustomAttribute("Question id", questionID));
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    //   Toast.makeText(this, "Story id is = "+storyID, Toast.LENGTH_SHORT).show();
                                }
                            } catch (Exception e) {

                                e.printStackTrace();
                            }


                        }


                        // Handle the deep link. For example, open the linked
                        // content, or apply promotional credit to the user's
                        // account.
                        // ...

                        // ...
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        initializeActivity();
                        Log.w("DeepLink", "getDynamicLink:onFailure", e);
                    }
                });
    }

    public void openTopicQuestionActivity(int check, String topicName, String QuestionUID) {

        Intent intent = new Intent(MainActivity.this, TopicQuestions.class);
        Bundle bundle = new Bundle();
        bundle.putInt("check", check);
        bundle.putString("Topic", topicName);
        bundle.putString("questionUID", QuestionUID);
        intent.putExtras(bundle);
        startActivity(intent);
    }


    BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.bottombar_main_topic:
                    downloadTopicList();
                    return true;

                case R.id.bottombar_main_daily:
                    downloadDateList();
                    return true;


            }
            return false;
        }
    };

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_test) {

            Intent intent = new Intent(MainActivity.this, RandomTestActivity.class);
            startActivity(intent);
            // Handle the camera action
        } else if (id == R.id.nav_bookmark) {
            Intent intent = new Intent(MainActivity.this, BookmarkActivity.class);
            startActivity(intent);

            // onRateUs();
        } else if (id == R.id.nav_rate) {
            onRateUs();
        } else if (id == R.id.nav_share) {

            ShareApp();
        } else if (id == R.id.nav_suggestion) {
            giveSuggestion();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void showDialog(String message) {
        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMessage(message);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    public void hideDialog() {
        progressDialog.cancel();
    }

    private void giveSuggestion() {

        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"acraftystudio@gmail.com"});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Suggestion For " + getResources().getString(R.string.app_name));
        emailIntent.setType("text/plain");
        startActivity(Intent.createChooser(emailIntent, "Send mail From..."));

    }

    private void onRateUs() {
        try {
            String link = "https://goo.gl/DesxPy";
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(link)));
        } catch (Exception e) {

        }
    }

    private void ShareApp() {

        try {
            /*
            Answers.getInstance().logCustom(new CustomEvent("Share link created").putCustomAttribute("Content Id", questions.getQuestionUID())
                    .putCustomAttribute("Shares", questions.getQuestionTopicName()));
            */
        } catch (Exception e) {
            e.printStackTrace();
        }
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");

        //sharingIntent.putExtra(Intent.EXTRA_STREAM, newsMetaInfo.getNewsImageLocalPath());

        hideDialog();

        String Link = "https://goo.gl/DesxPy";
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, Link + "\n Logical Reasoning App \n Download it Now! ");
        startActivity(Intent.createChooser(sharingIntent, "Share Logical Reasoning Question via"));

    }

}
