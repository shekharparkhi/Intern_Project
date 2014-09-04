/**
 * PayPalHereSDK
 * <p/>
 * Created by PayPal Here SDK Team.
 * Copyright (c) 2013 PayPal. All rights reserved.
 */
package com.paypal.sampleapp.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import com.paypal.merchant.sdk.MerchantManager;
import com.paypal.merchant.sdk.PayPalHereSDK;
import com.paypal.merchant.sdk.TransactionManager;
import com.paypal.merchant.sdk.TransactionManager.PaymentResponse;
import com.paypal.merchant.sdk.domain.CheckedInClient;
import com.paypal.merchant.sdk.domain.DefaultResponseHandler;
import com.paypal.merchant.sdk.domain.Merchant;
import com.paypal.merchant.sdk.domain.PPError;
import com.paypal.sampleapp.R;
import com.paypal.sampleapp.adapter.ClientGridViewAdapter;
import com.paypal.sampleapp.adapter.ListAdapter;
import com.paypal.sampleapp.util.CommonUtils;

import java.util.List;

/**
 * This activity is meant to take a payment via a checked in customer.
 * In order to take a payment, both the merchant and customer need to be checked in.
 * <p/>
 * The scenario would be as follows:
 * <p/>
 * 1. Merchant checks in to a location.
 * <p/>
 * 2. If the customer wants to make a payment via the check in methodology, they too would need to check in to the
 * merchant's location.
 * <p/>
 * 3. The merchant could then retrieve this checked in customer and take a payment via that.
 */
public class PayPalMerchantCheckinActivity extends MyActivity {

    private static final String LOG = "PayPalHere.PayPalMerchantCheckinActivity";
    private static MerchantManager sMerchantManager = PayPalHereSDK.getMerchantManager();

    /**
     * Implementing a PaymentResponseHandler to handle the response status of a
     * transaction.
     */

    DefaultResponseHandler<PaymentResponse, PPError<TransactionManager.PaymentErrors>> mPaymentResponseHandler = new
            DefaultResponseHandler<PaymentResponse, PPError<TransactionManager.PaymentErrors>>() {
                public void onSuccess(PaymentResponse response) {
                    updateUIForPurchaseSuccess(response);
                    mClientGridView.setVisibility(View.GONE);
                    mSearchForCheckedInClientsButton.setVisibility(View.GONE);
                    paymentCompleted(true);

                }

                @Override
                public void onError(PPError<TransactionManager.PaymentErrors> e) {
                    updateUIForPurchaseError(e);
                    paymentCompleted(false);
                }
            };

    private ProgressDialog mProgressDialog;
    private GridView mClientGridView;
    private ListAdapter<List<CheckedInClient>> mGridViewAdapter;
    private Button mSearchForCheckedInClientsButton;
    private String mLocationId;
    private Merchant mActiveMerchant;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        // Set the layout for this activity.
        setContentView(R.layout.activity_paypal_merchant_checkin);

        mProgressDialog = new ProgressDialog(PayPalMerchantCheckinActivity.this);
        mProgressDialog.setIndeterminate(true);

        // Get the merchant object from the SDK.
        mActiveMerchant = PayPalHereSDK.getMerchantManager().getActiveMerchant();

        mClientGridView = (GridView) findViewById(R.id.gridview);
        mClientGridView.setVisibility(View.VISIBLE);
        mClientGridView.setEmptyView(findViewById(R.id.empty_checkedin_clients));

        getCheckedInClientList(); // Auto search checkin on Activity start

        // Find and set the button when the merchant tries to search for a list of checked in customers.
        mSearchForCheckedInClientsButton = (Button) findViewById(R.id.search_for_clients_button);
        mSearchForCheckedInClientsButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                getCheckedInClientList();
            }
        });
        mSearchForCheckedInClientsButton.setVisibility(View.VISIBLE);

        paymentCompleted(false);
    }

    private void showProgressDialog(String msg) {
        mProgressDialog.setMessage(msg);
        mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog.isShowing())
            mProgressDialog.dismiss();
    }

    /**
     * This method is called in order to perform another transaction after the completion of the current one.
     *
     *  // DELETED
     *
     */


    /**
     * This method retrieves the list of checked in customers by invoking an API in the SDK.
     */

    private void getCheckedInClientList() {
        if (!isMerchantCheckedIdIn()) {
            CommonUtils.createToastMessage(PayPalMerchantCheckinActivity.this,
                    "Cannot perform this action. Merchant is not checked in.");
            return;
        }

        Log.d(LOG, mActiveMerchant.getCheckedInMerchant().getLocationId());

        showProgressDialog("Retrieving checked in people ..... ");
        // Invoke the API to retrieve the list of checked in customers.
        // Provide a handler in order to receive the response.
        PayPalHereSDK.getMerchantManager().getCheckedInClientsList(new DefaultResponseHandler<List<CheckedInClient>,
                PPError<MerchantManager.MerchantErrors>>() {
            // if the list was obtained successfully, display on the UI screen.
            @Override
            public void onSuccess(List<CheckedInClient> arg0) {
                displayClientList(arg0);
                hideProgressDialog();
            }

            @Override
            public void onError(PPError<MerchantManager.MerchantErrors> arg0) {
                CommonUtils.createToastMessage(PayPalMerchantCheckinActivity.this, "Error while retrieving the list.");
                hideProgressDialog();
            }
        });
    }

    /**
     * This method is called to display the list of checked in clients on the UI.
     *
     * @param clientList
     */

    private void displayClientList(final List<CheckedInClient> clientList) {
        mGridViewAdapter = new ClientGridViewAdapter(this, clientList);
        mClientGridView.setAdapter(mGridViewAdapter);

        mClientGridView.setOnItemClickListener(new OnItemClickListener() {
            // If a customer is selected, get the "CheckedInClient" object and use the same to take the payment.

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                CheckedInClient client = clientList.get(position);
                CommonUtils.createToastMessage(PayPalMerchantCheckinActivity.this, "Payment Processing! Please Wait ..... ");
                takePaymentWithCheckedInClient(client);
            }

        });
    }

    /**
     * This method is invoked to take a payment with the checked in customer.
     *
     * @param client
     */

    private void takePaymentWithCheckedInClient(CheckedInClient client) {


        displayPaymentState("Taking payment ..... ");
        // Invoke the API with the checked in customer to take the payment.

        // **NOTE**: The transaction state i.e., the invoice, the transaction extras,
        // previously read credit cards etc would be kept intact only between the begin - finalize payments. If the
        // payment goes through successfully or if it returns back with a failure,
        // all the above mentioned objects are removed and the app would need to call beginPayment once again to
        // re-init, set the invoice back and try again.
        PayPalHereSDK.getTransactionManager().processPayment(client, null, mPaymentResponseHandler);
    }

    private void updateUIForPurchaseError(PPError<TransactionManager.PaymentErrors> e) {
        TransactionManager.PaymentErrors error_type = e.getErrorCode();

        if (TransactionManager.PaymentErrors.PaymentDeclined == error_type) {
            displayPaymentState("Payment declined!  Payment cycle complete.  Please start again");
        } else if (TransactionManager.PaymentErrors.NetworkTimeout == error_type) {
            displayPaymentState("Payment timed out at network level.");
        } else if (TransactionManager.PaymentErrors.NoDeviceForCardPresentPayment == error_type) {
            displayPaymentState("No Device connected.  Connect your device.");
        } else if (TransactionManager.PaymentErrors.NoPaymentInfoPresent == error_type) {
            displayPaymentState("We can't take card payment ... no card has been scanned.");
        } else if (TransactionManager.PaymentErrors.TimeoutWaitingForSwipe == error_type) {
            displayPaymentState("Payment Canceled.  Expecting card swipe but no swipe ever happened");
        } else if (TransactionManager.PaymentErrors.BadConfiguration == error_type) {
            displayPaymentState("Payment Canceled.  Incorrect Usage / Bad Configuration " + e.getDetailedMessage());
        } else if (TransactionManager.PaymentErrors.EmptyShoppingCart == error_type) {
            displayPaymentState("You've got an empty invoice, or an invoice with zero value.  Can't process payment");
        } else if (TransactionManager.PaymentErrors.NetworkError == error_type) {
            displayPaymentState("No network availability.  Can't process anything");
        } else {
            displayPaymentState("Unhandled error: " + e.getDetailedMessage());
        }
    }

    private void updateUIForPurchaseSuccess(TransactionManager.PaymentResponse response) {
        displayPaymentState("Payment completed successfully!  \nTransactionId: " + response.getTransactionRecord()
                .getTransactionId());

        TextView tv = (TextView) findViewById(R.id.checkout_text);
        tv.setVisibility(View.GONE);

        new Handler().postDelayed(new Runnable() {
            public void run() {

                Intent intent = new Intent();
                intent.setClass(PayPalMerchantCheckinActivity.this, ThanksActivity.class);

                PayPalMerchantCheckinActivity.this.startActivity(intent);
                PayPalMerchantCheckinActivity.this.finish();

                overridePendingTransition(R.anim.fadein, R.anim.fadeout);

            }
        }, 5000);

    }

    private void displayPaymentState(String state) {
        Log.d(LOG, "state: " + state);
        TextView tv = (TextView) findViewById(R.id.purchase_status_checkin);
        tv.setText(state);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (PayPalHereSDK.getTransactionManager().isProcessingAPayment() || isPaymentCompleted()) {
            mClientGridView.setVisibility(View.GONE);
            mSearchForCheckedInClientsButton.setVisibility(View.GONE);

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        boolean returnValue;

        switch (item.getItemId()) {
            case R.id.checkin:

                sMerchantManager.checkinMerchant("Beer @ Work Day!",
                        new DefaultResponseHandler<Merchant, PPError<MerchantManager.MerchantErrors>>() {

                            @Override
                            public void onSuccess(Merchant merchant) {
                                CommonUtils.createToastMessage(PayPalMerchantCheckinActivity.this,
                                        "Merchant checkin successful!");
                                Log.e(LOG, "Checkin successful");

                            }

                            @Override
                            public void onError(PPError<MerchantManager.MerchantErrors> error) {
                                CommonUtils.createToastMessage(PayPalMerchantCheckinActivity.this,
                                        "Checkin unsuccessful. " + error.getDetailedMessage());

                                Log.e(LOG, "Checkin unsuccessful. " + error.getDetailedMessage());
                            }
                        }
                );

                returnValue = true;

                break;

            case R.id.checkout:

                sMerchantManager.checkoutMerchant(new DefaultResponseHandler<Merchant,
                        PPError<MerchantManager.MerchantErrors>>() {
                    @Override
                    public void onSuccess(Merchant merchant) {
                        CommonUtils.createToastMessage(PayPalMerchantCheckinActivity.this,
                                "Merchant checkout successful!");
                        Log.d(LOG, "Checkout successful");
                    }

                    @Override
                    public void onError(PPError<MerchantManager.MerchantErrors> error) {
                        CommonUtils.createToastMessage(PayPalMerchantCheckinActivity.this,
                                "Merchant checkout unsuccessful!");
                        Log.e(LOG, "Checkout unsuccessful. " + error.getDetailedMessage());
                    }
                });

                returnValue = true;

                break;

            default:
                returnValue = super.onOptionsItemSelected(item);

        }
        return returnValue;

    }
}
