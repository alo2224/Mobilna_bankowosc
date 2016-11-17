package com.example.alo.mobilna_bankowosc;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.opengl.GLSurfaceView;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This is the main activity class that displays the Account Status,
 * operations history and allows user to send a transfer
 * using one of his connected accounts.
 */
public class MainActivity extends AppCompatActivity {
    TableLayout table;
    ScrollView scroll;
    LinearLayout mainLayout;
    LinearLayout wholeLayout;
    LinearLayout dataContainer;
    String user;
    /**
     * Contains a list of banks that user has logged to.
     */
    List connectedBanks = new ArrayList();
    HashMap<String,String> banksData  = new HashMap<String,String>();
    /**
     * Contains a list of user accounts
     */
    List accountsList = new ArrayList();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Bundle extras = getIntent().getExtras();
        this.user = "pols@wp.pl";
        if(extras != null) {
            this.user=extras.getString("user");
        }
        getConnectedBanks();
        try {
            getAccountList();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Button operationHistory = (Button) findViewById(R.id.operationHistory);
        operationHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectAccount();
            }
        });
        Button transfer = (Button) findViewById(R.id.transfer);
        transfer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createTransferView();
            }
        });
        Button accountStatus = (Button) findViewById(R.id.accountStatus);
        accountStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    displayAccountsStatus();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        mainLayout = (LinearLayout) findViewById(R.id.mainLayout);
        wholeLayout = (LinearLayout) findViewById(R.id.wholeLayout);
        scroll = new ScrollView(this);
        table = new TableLayout(this);
        dataContainer = new LinearLayout(this);
        TableLayout.LayoutParams llp = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT);
        ScrollView.LayoutParams llps = new ScrollView.LayoutParams(ScrollView.LayoutParams.MATCH_PARENT,ScrollView.LayoutParams.MATCH_PARENT);
        LinearLayout.LayoutParams llpm = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT);
        dataContainer.setLayoutParams(llpm);
        dataContainer.setOrientation(LinearLayout.VERTICAL);
        table.setLayoutParams(llp);
        scroll.setLayoutParams(llps);
        accountStatus.performClick();
    }

    /**
     * Displays accounts status for all the connected banks
     * @throws JSONException
     */
    protected void displayAccountsStatus() throws JSONException {
        mainLayout.removeAllViews();
        scroll.removeAllViews();
        dataContainer.removeAllViews();
        mainLayout.addView(scroll);
        if(wholeLayout.getChildAt(1) != null && wholeLayout.getChildAt(1) instanceof ImageView){
            //The image is added at first position so we need to remove it before replacing
            wholeLayout.removeViewAt(1);
        }
        int numberOfBanks = connectedBanks.size();
        JSONObject  accountsData = new JSONObject();
        String bankId = " ";
        for(int i=0;i<numberOfBanks;i++){
            bankId = (String) connectedBanks.get(i);
            addBankLogo(bankId);
            try {
                getAccountHistory job = new getAccountHistory();
                String data =  job.execute(bankId).get();
                accountsData = new JSONObject(data);
                accountsData = accountsData.getJSONObject("accounts");
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            Iterator<?> keys = accountsData.keys();
            while(keys.hasNext()){
                String key = (String)keys.next();
                String accountNumber = key;
                TextView accNumber = new TextView(this);
                TextView acc = new TextView(this);
                ViewGroup.LayoutParams lp = new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT , ViewGroup.LayoutParams.WRAP_CONTENT);
                accNumber.setLayoutParams(lp);
                accNumber.setGravity(Gravity.CENTER);
                accNumber.setBackgroundColor(Color.WHITE);
               // accNumber.setText();
                acc.setBackgroundColor(Color.WHITE);
                acc.setLayoutParams(lp);
                acc.setText("Konto: " + prettyPrintAccount(accountNumber));
                dataContainer.addView(acc);
                //dataContainer.addView(accNumber);
                if(accountsData.get(key) instanceof JSONObject) {
                    JSONObject accountData = accountsData.getJSONObject(key);
                    String balance = accountData.get("balance").toString();
                    double balanceInt = Double.parseDouble(balance);
                    TextView balanceView = new TextView(this);
                    TextView balanceStatus = new TextView(this);
                    balanceView.setLayoutParams(lp);
                    balanceStatus.setLayoutParams(lp);
                    balanceView.setBackgroundColor(Color.WHITE);
                    balanceView.setText("Stan: ");
                    balanceStatus.setText(balance);
                    balanceView.setTypeface(Typeface.DEFAULT_BOLD);
                    balanceStatus.setTypeface(Typeface.DEFAULT_BOLD);
                    balanceView.setTextSize(15);
                    balanceStatus.setTextSize(25);
                    balanceStatus.setGravity(Gravity.RIGHT);
                    if(balanceInt > 0){
                        balanceStatus.setTextColor(Color.rgb(45,110,15));
                    }
                    else if(balanceInt < 0){
                        balanceStatus.setTextColor(Color.RED);
                    }
                    dataContainer.addView(balanceView);
                    dataContainer.addView(balanceStatus);
                    View v = new View(this);
                    v.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 2));
                    v.setBackgroundColor(Color.BLACK);
                    dataContainer.addView(v);
                }
            }
            View v = new View(this);
            v.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 2));
            v.setBackgroundColor(Color.BLACK);
            v.setPadding(0,0,0,10);
            dataContainer.addView(v);
        }
        scroll.addView(dataContainer);
    }

    /**
     * Creates a view that allows the end user to send a transfer
     * It also validates its data
     */
    protected void createTransferView(){
        mainLayout.removeAllViews();
        //There will be second child only when the image is added
        if(wholeLayout.getChildAt(2) != null && wholeLayout.getChildAt(1) instanceof ImageView){
            //The image is added at first position so we need to remove it before replacing
            wholeLayout.removeViewAt(1);
        }
        final EditText senderAccount = new EditText(this);
        ViewGroup.LayoutParams lp = new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT , ViewGroup.LayoutParams.WRAP_CONTENT);
        senderAccount.setHint("Konto");
        senderAccount.setInputType(InputType.TYPE_CLASS_NUMBER);
        senderAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                final CharSequence [] accountsArray = (CharSequence[]) accountsList.toArray(new CharSequence[connectedBanks.size()]);
                builder.setTitle("Wybierz konto");
                builder.setItems(accountsArray, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // the user clicked on colors[which]
                        senderAccount.setText((CharSequence) accountsArray[which]);
                    }
                });
                builder.show();
            }
        });
        final EditText reciverData = new EditText(this);
        reciverData.setLayoutParams(lp);
        reciverData.setHint("Dane odbiorcy");
        final EditText reciverAccountNumber = new EditText(this);
        reciverAccountNumber.setInputType(InputType.TYPE_CLASS_NUMBER);
        reciverAccountNumber.setHint("Number konta odbiorcy");
        reciverAccountNumber.setLayoutParams(lp);
        final EditText amount = new EditText(this);
        amount.setRawInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        amount.setLayoutParams(lp);
        amount.setHint("Kwota");
        final EditText transferTitle = new EditText(this);
        transferTitle.setInputType(InputType.TYPE_CLASS_TEXT);
        transferTitle.setHint("Tytul przelewu");
        transferTitle.setLayoutParams(lp);
        Button sendTransfer = new Button(this);
        sendTransfer.setText("Wyslij");
        sendTransfer.setLayoutParams(lp);
        sendTransfer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View focusView = null;
                boolean cancel = false;
                String accountNumber = " ";
                String convertedAmount = " ";
                String transferTitleString = " ";
                String reciverDataString = " ";
                String senderAccountNumber = " ";
                if(senderAccount.getText().toString().isEmpty()){
                    senderAccount.setError("To pole jest wymagane");
                    focusView = senderAccount;
                    focusView.requestFocus();
                    cancel = true;
                }
                 else {
                    if (senderAccount.getText().toString().length() > 26) {
                        senderAccount.setError("Numer konta jest za dlugi");
                        focusView = senderAccount;
                        focusView.requestFocus();
                        cancel = true;
                    } else if (senderAccount.getText().toString().length() < 26) {
                        senderAccount.setError("Numer konta jest za krotki");
                        focusView = senderAccount;
                        focusView.requestFocus();
                        cancel = true;
                    } else {
                        senderAccountNumber = senderAccount.getText().toString();
                    }
                }
                if (reciverAccountNumber.getText().toString().isEmpty()) {
                    reciverAccountNumber.setError("To pole jest wymagane");
                    focusView = reciverAccountNumber;
                    focusView.requestFocus();
                    cancel = true;
                } else {
                    if (reciverAccountNumber.getText().toString().length() > 26) {
                        reciverAccountNumber.setError("Numer konta jest za dlugi");
                        focusView = reciverAccountNumber;
                        focusView.requestFocus();
                        cancel = true;
                    } else if (reciverAccountNumber.getText().toString().length() < 26) {
                        reciverAccountNumber.setError("Numer konta jest za krotki ");
                        focusView = reciverAccountNumber;
                        focusView.requestFocus();
                        cancel = true;
                    } else {
                        accountNumber = reciverAccountNumber.getText().toString();
                    }
                }
                if (amount.getText().toString().isEmpty()) {
                    amount.setError("To pole jest wymagane");
                    focusView = amount;
                    focusView.requestFocus();
                    cancel = true;
                } else {
                    convertedAmount = amount.getText().toString().replace(",", ".");
                    Log.i("amm", convertedAmount);
                    //convertedAmount = convertedAmount.replace(".",".");
                    float transferAmount = Float.parseFloat(convertedAmount);
                    Log.i("123","123");
                    Log.i("amm", String.valueOf(transferAmount));
                    if (transferAmount < 0) {
                        amount.setError("Wartosc przelewu nie moze byc ujemna");
                        focusView = amount;
                        focusView.requestFocus();
                        cancel = true;
                    } else {
                        DecimalFormat df = new DecimalFormat();
                        df.setMaximumFractionDigits(2);
                        String ammount = df.format(transferAmount).toString();
                        ammount = ammount.replace(",", ".");
                        transferAmount = Float.parseFloat(ammount);
                        convertedAmount = String.valueOf(transferAmount);
                    }
                }
                if (transferTitle.getText().toString().isEmpty()) {
                    transferTitle.setError("To pole jest wymagane");
                    focusView = transferTitle;
                    focusView.requestFocus();
                    cancel = true;
                } else {
                    transferTitleString = transferTitle.getText().toString();
                }
                if (reciverData.getText().toString().isEmpty()) {
                    reciverData.setError("To pole jest wymagane");
                    focusView = reciverData;
                    focusView.requestFocus();
                    cancel = true;
                } else {
                    reciverDataString = reciverData.getText().toString();
                }
                if (!cancel) {
                    final String finalReciverDataString = reciverDataString;
                    final String finalTransferTitleString = transferTitleString;
                    final String finalConvertedAmount = convertedAmount;
                    final String finalAccountNumber = accountNumber;
                    final String finalSenderAccountNumber = senderAccountNumber;
                    new AlertDialog.Builder(wholeLayout.getContext())
                            .setMessage("Czy dane sa poprawne? \nTwoj numer konta: " +  prettyPrintAccount(finalSenderAccountNumber) + "\nDane odbiorcy: " + finalReciverDataString + "\nNumber konta odbiorcy: " + prettyPrintAccount(finalAccountNumber) + "\nTytul przelewu: " + finalTransferTitleString + "\nKwota: " + finalConvertedAmount)
                            .setCancelable(false)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    JSONObject transferRequest = new JSONObject();
                                    insertIntoAccountHistory test = new insertIntoAccountHistory();
                                    Date todaysDate = new Date();
                                    test.execute(finalSenderAccountNumber, "27.01.2016", "15:56", finalReciverDataString, finalTransferTitleString, "-" + finalConvertedAmount);
                                    try {
                                        transferRequest.put("senderAccountNumber", finalSenderAccountNumber);
                                        transferRequest.put("reciverData", finalReciverDataString);
                                        transferRequest.put("transferTitle", finalTransferTitleString);
                                        transferRequest.put("transferAmount", finalConvertedAmount);
                                        transferRequest.put("reciverAccount", finalAccountNumber);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    senderAccount.getText().clear();
                                    amount.getText().clear();
                                    reciverData.getText().clear();
                                    reciverAccountNumber.getText().clear();
                                    transferTitle.getText().clear();
                                    new AlertDialog.Builder(wholeLayout.getContext())
                                            .setMessage("Przelew został wysłany")
                                            .setPositiveButton("Ok", null)
                                            .show();
                                }
                            })
                            .setNegativeButton("No", null)
                            .show();
                }
            }
        });
        mainLayout.addView(senderAccount);
        mainLayout.addView(reciverData);
        mainLayout.addView(reciverAccountNumber);
        mainLayout.addView(transferTitle);
        mainLayout.addView(amount);
        mainLayout.addView(sendTransfer);
    }

    /**
     * Creates a pop-up that enables user to select for which bank he wants to see the operation history.
     */
    protected void selectAccount() {
        final CharSequence [] banksIds = (CharSequence[]) connectedBanks.toArray(new CharSequence[connectedBanks.size()]);
        final String[] selectedAccount = new String[1];
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Wybierz bank");
        builder.setItems(banksIds, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // the user clicked on colors[which]
                selectedAccount[0] = (String) banksIds[which];
                try {
                    displayOperationHistory(selectedAccount[0]);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        builder.show();
    }

    /**
     * Gets the list of all bank that the user has connected to
     */
    protected void getConnectedBanks() {
        try {
            File file = new File(getApplicationContext().getFilesDir(), user);
            BufferedReader br = new BufferedReader(new FileReader(file));
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();
            //connectedBanks.add("mBank");
            //connectedBanks.add("PKO");
            while (line != null) {
                String [] parts = line.split(";");
                connectedBanks.add(parts[0]);
                banksData.put(parts[0],parts[1]);
                sb.append(line);
                line = br.readLine();
            }
            String everything = sb.toString();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets the list of all user accounts ( one user can have multiple accounts in one bank )
     * @throws JSONException
     */
    protected void getAccountList() throws JSONException {
        int noOfBanks = connectedBanks.size();
        JSONObject accountsData = new JSONObject();

        for(int i=0;i<noOfBanks;i++){
            getAccountHistory job = new getAccountHistory();
            try {
                String data =  job.execute((String) connectedBanks.get(i)).get();
                accountsData = new JSONObject(data);
                accountsData = accountsData.getJSONObject("accounts");
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            Iterator<?> keys = accountsData.keys();
            while(keys.hasNext()) {
                String key = (String) keys.next();
                if (accountsData.get(key) instanceof JSONObject) {
                    accountsList.add(key);
                }
            }
        }
    }

    /**
     * Displays the history of operations for certain bank provided as parameter
     * @param bankId
     * @throws JSONException
     */
    protected void displayOperationHistory(String bankId) throws JSONException {
        table.removeAllViews();
        scroll.removeAllViews();
        mainLayout.removeAllViews();
        dataContainer.removeAllViews();
        addBankLogo(bankId);
        dataContainer.addView(table);
        scroll.addView(dataContainer);
        mainLayout.addView(scroll);
        getAccountHistory job = new getAccountHistory();
        JSONObject accounts = new JSONObject();
        try {
            String data =  job.execute(bankId).get();
            accounts = new JSONObject(data);
            accounts = accounts.getJSONObject("accounts");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        Iterator<?> keys = accounts.keys();
        while(keys.hasNext()){
            String key = (String)keys.next();
            if(accounts.get(key) instanceof JSONObject){
                createAccountIdRow(key);
                JSONObject account = accounts.getJSONObject(key);
                Integer accountBalance = account.getInt("balance");
                if(account.get("history") instanceof JSONObject){
                    JSONObject history = account.getJSONObject("history");
                    Iterator<?> keys1 = history.keys();
                    while(keys1.hasNext()){
                        String key1 = (String) keys1.next();
                        if(history.get(key1) instanceof JSONObject){
                            HashMap<String,String> textMap = new HashMap<>();
                            JSONObject transaction = history.getJSONObject(key1);
                            String transactionDate = (String) transaction.get("date");
                            String transactionTime = (String) transaction.get("time");
                            createTableRow(transactionDate);
                            createTableRow("Time",  transactionTime);
                            Double transactionAmount = transaction.getDouble("amount");
                            createTableRow("Amount", transactionAmount.toString());
                            String transactionInfo = (String) transaction.get("info");
                            createTableRow("Info", transactionInfo);
                            String transactionCategory = (String) transaction.get("category");
                            createTableRow("Category", transactionCategory);
                            String transactionType = (String) transaction.get("type");
                            createTableRow("Type", transactionType);
                        }
                        else{
                            continue;
                        }
                    }
                }
                else{
                    continue;
                }
            }
            else{
                continue;
            }
        }
    }

    /**
     * An asynchronus task that gets the account data from my database
     */
    private class getAccountHistory extends AsyncTask<String, Void, String> {
        JSONObject accounts = new JSONObject();
        @Override
        protected String doInBackground(String[] params) {
            String response = "";
            DefaultHttpClient client = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet("http://pluton.kt.agh.edu.pl/~azalenski/jpwp_test.php?bank=" + params[0]);
            try {
                HttpResponse execute = client.execute(httpGet);
                InputStream content = execute.getEntity().getContent();

                BufferedReader buffer = new BufferedReader(
                        new InputStreamReader(content));
                String s = "";
                while ((s = buffer.readLine()) != null) {
                    response += s;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        return response;
        }
        @Override
        protected void onPostExecute(String message) {
            //process message
            JSONObject jsonData = new JSONObject();

            String accData = " ";
            String [] operation  = message.split(";");
            for(String ops : operation) {
                try {
                    jsonData = new JSONObject(ops);
                    accounts = jsonData.getJSONObject("accounts");
                    accData = jsonData.get("accounts").toString();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * An asynchronous task that inserts data about transfer into the database
     */
    private class insertIntoAccountHistory extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String[] params) {
            String response = "";
            DefaultHttpClient client = new DefaultHttpClient();
            String urlStr = "http://pluton.kt.agh.edu.pl/~azalenski/jpwp_insert.php?konto=" + params[0] + "&data=" + params[1] + "&time=" + params [2] + "&info=" + params[3] + "&category=" + params [4] + "&amount=" + params[5];
            URL url = null;
            try {
                url = new URL(urlStr);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            URI uri = null;
            try {
                uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            String request = uri.toString();
            HttpGet httpGet = new HttpGet(request);
            try {
                HttpResponse execute = client.execute(httpGet);
                InputStream content = execute.getEntity().getContent();

                BufferedReader buffer = new BufferedReader(
                        new InputStreamReader(content));
                String s = "";
                while ((s = buffer.readLine()) != null) {
                    response += s;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return response;
        }

        @Override
        protected void onPostExecute(String message) {
            //process message
        }
    }

    /**
     * Creates a account number row in the operation history tab
     * @param accountId
     */
    protected  void createAccountIdRow(String accountId){
        TableRow tr = new TableRow(this);
        //tr.setBackgroundColor(Color.BLACK);
        tr.setPadding(4, 20, 4, 20); //Border between rows
        TableRow.LayoutParams llp = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT);
        llp.setMargins(0,20,0,20);
        tr.setLayoutParams(llp);
        TableRow.LayoutParams llp1 = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
        TableRow.LayoutParams llp2 = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
        llp2.weight = 10;
        //llp2.gravity = Gravity.RIGHT;
        LinearLayout cell1 = new LinearLayout(this.getApplicationContext());
        LinearLayout cell2 = new LinearLayout(this.getApplicationContext());
        cell1.setBackgroundColor(Color.WHITE);
        cell2.setBackgroundColor(Color.WHITE);
        cell2.setGravity(Gravity.RIGHT);
        cell1.setLayoutParams(llp1);//2px border on the right for the cell
        cell2.setLayoutParams(llp2);//2px border on the right for the cell
        TextView tv1 = new TextView(this);
        tv1.setText("Account:");
        TextView tv2 = new TextView(this);
        String result = prettyPrintAccount(accountId);
        tv2.setText(result);
        tv2.setTypeface(Typeface.DEFAULT_BOLD);
        tv2.setTextColor(Color.BLACK);
        tv1.setPadding(0, 0, 4, 3);
        tv2.setPadding(0, 0, 4, 3);
        cell1.addView(tv1);
        cell2.addView(tv2);
        tr.addView(cell1);
        tr.addView(cell2);
        table.addView(tr);
    }

    /**
     * Splits the account number into parts so they are easily readable by human and returns it.
     * @param accountNumber
     * @return
     */
    protected String prettyPrintAccount(String accountNumber){
        String begining = accountNumber.substring(0,2);
        String rest = accountNumber.substring(2);
        int interval = 4;
        char separator = ' ';
        StringBuilder sb = new StringBuilder(rest);
        for(int i = 0; i < rest.length() / interval; i++) {
            sb.insert(((i + 1) * interval) + i, separator);
        }
        String result = sb.toString();
        result = begining + " " + result;
        return result;
    }

    /**
     * Adds a bank logo image into any view
     * @param bankId
     */
    protected void addBankLogo(String bankId){
        HashMap<String,Integer> hmap = new HashMap<String,Integer>();
        hmap.put("mBank", R.drawable.mbank);
        hmap.put("WBK", R.drawable.wbk);
        hmap.put("Alior", R.drawable.alior);
        hmap.put("PKO", R.drawable.pko);
        ImageView bankLogo = new ImageView(this);
        bankLogo.setImageResource(hmap.get(bankId));
        bankLogo.setPadding(0,10,0,0);
        //insert the image after buttons
        dataContainer.addView(bankLogo);
    }

    /**
     * Creates a table row with one column - date in the operation history tab
     * @param string
     */
    protected  void createTableRow(String string){
        TableRow tr = new TableRow(this);
        tr.setPadding(0, 0, 0, 2);
        TableRow.LayoutParams llp = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT);
        llp.weight = 10;
        llp.setMargins(0, 0, 2, 0);//2px right-margin
        LinearLayout cell = new LinearLayout(this.getApplicationContext());
        //cell.setBackgroundColor(Color.WHITE);
        cell.setLayoutParams(llp);//2px border on the right for the cell
        TextView tv = new TextView(this);
        tv.setText(string);
        tv.setTypeface(Typeface.DEFAULT_BOLD);
        tv.setTextColor(Color.BLACK);
        tv.setPadding(0, 0, 4, 3);
        cell.addView(tv);
        tr.addView(cell);
        table.addView(tr);
    }

    /**
     * Creates table row with two columnt - all other parameters in the operation history tab
     * @param string1
     * @param string2
     */
    protected void createTableRow(String string1, String string2){
        TableRow tr = new TableRow(this);
        tr.setBackgroundColor(Color.BLACK);
        tr.setPadding(2, 2, 2, 2); //Border between rows
        TableRow.LayoutParams llp = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT);
        tr.setLayoutParams(llp);
        TableRow.LayoutParams llp1 = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
        TableRow.LayoutParams llp2 = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
        llp1.weight = 10;
        //llp2.gravity = Gravity.RIGHT;
        LinearLayout cell1 = new LinearLayout(this.getApplicationContext());
        LinearLayout cell2 = new LinearLayout(this.getApplicationContext());
        cell1.setBackgroundColor(Color.WHITE);
        cell1.setLayoutParams(llp1);//2px border on the right for the cell
        cell2.setLayoutParams(llp2);//2px border on the right for the cell
        TextView tv1 = new TextView(this);
        tv1.setText(string1);
        TextView tv2 = new TextView(this);
        if(string1.contains("Amount")){
            Double amount = Double.valueOf(string2);
            int decimal = Integer.parseInt(string2.split("\\.")[0]);
            int fractional = Integer.parseInt(string2.split("\\.")[1]);
            tv1.setTextColor(Color.BLACK);
            tv2.setTextColor(Color.BLACK);
            tv2.setTypeface(Typeface.DEFAULT_BOLD);
            if(amount < 0){
                cell1.setBackgroundColor(Color.RED);
                cell2.setBackgroundColor(Color.RED);
            }
            else{
                cell1.setBackgroundColor(Color.rgb(103,227,61));
                cell2.setBackgroundColor(Color.rgb(103,227,61));
            }
        }
        else{
            cell2.setBackgroundColor(Color.WHITE);
        }

        tv2.setGravity(Gravity.RIGHT);
        tv2.setText(string2);
        tv1.setPadding(0, 0, 4, 3);
        tv2.setPadding(0, 0, 4, 3);
        cell1.addView(tv1);
        cell2.addView(tv2);
        tr.addView(cell1);
        tr.addView(cell2);
        table.addView(tr);
    }
}
