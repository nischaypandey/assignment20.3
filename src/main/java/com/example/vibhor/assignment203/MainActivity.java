package com.example.vibhor.assignment203;

import android.Manifest;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.OperationApplicationException;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;

//creating class by extending AppCompatActivity.
public class MainActivity extends AppCompatActivity
{
    //Creating references of elements used in the layout.
    Button deleteBtn;
    EditText numberToDelete;
    ContentResolver contentResolver;
    private static final int PERMISSIONS_REQUEST_DELETE_CONTACTS = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        //Setting Content View.
        setContentView(R.layout.activity_main);

        //Getting Content Resolver
        contentResolver= getContentResolver();

        //Setting references with their IDs.
        deleteBtn=(Button)findViewById(R.id.delete_btn);
        numberToDelete=(EditText)findViewById(R.id.delete_number_et);

        //Setting onClick listener to button.
        deleteBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //Calling Method to Delete the Contact.
                deleteItem();
            }
        });
    }

    //Method to delete.
    private void deleteItem()
    {
        //Checking that if version is greater than or equal to Marshmallow, if then requesting for permission.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED)
        {
            requestPermissions(new String[]{Manifest.permission.WRITE_CONTACTS}, PERMISSIONS_REQUEST_DELETE_CONTACTS);
        }

        //Checking that all Details are fiiled.
        if(!numberToDelete.getText().toString().isEmpty())
        {
            //calling delete() method.
            delete(contentResolver,numberToDelete.getText().toString());
            Toast.makeText(getApplicationContext(),"Contact Deleted",Toast.LENGTH_SHORT).show();
        }
        else
        {
            //Displaying Toast.
            Toast.makeText(getApplicationContext(),"Please Fill Correct Phone Nuumber",Toast.LENGTH_SHORT).show();
        }
    }

    //update method.
    public static void delete(ContentResolver contentResolver,String numberToDelete)
    {
        //ArrayList of ContentProviderOperation class.
        ArrayList<ContentProviderOperation> operationArrayList = new ArrayList<>();

        //Fetching contactID of first contact.
        String contactID = String.valueOf(getContactID(contentResolver,numberToDelete));



        //Creating Arguements array.
        String[] phoneArgs = new String[]{contactID};

        //deleting Contact.
        operationArrayList.add(ContentProviderOperation.newDelete(ContactsContract.RawContacts.CONTENT_URI)
                .withSelection(ContactsContract.RawContacts._ID + "=?",phoneArgs)
                .build());

        try
        {
            //Applaying batch to ContentResolver.
            contentResolver.applyBatch(ContactsContract.AUTHORITY,operationArrayList);
        }
        catch (RemoteException e)
        {
            e.printStackTrace();
        }
        catch (OperationApplicationException e)
        {
            e.printStackTrace();
        }

    }

    //Method to get ID of Contact.
    private static long getContactID(ContentResolver contentResolver,String number)
    {
        //Creating Uri.
        Uri contactUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));

        //Creating projection.
        String[] projection = { ContactsContract.PhoneLookup._ID };

        //creating reference of Cursor.
        Cursor cursor=null;

        try
        {
            //creating cursor by query.
            cursor = contentResolver.query(contactUri, projection, null, null,null);

            if (cursor != null && cursor.moveToFirst())
            {
                //Fetching ID and returning ID.
                int personID = cursor.getColumnIndex(ContactsContract.PhoneLookup._ID);
                return cursor.getLong(personID);
            }

            return -1;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (cursor != null) {
                cursor.close();
            }
        }
        return -1;
    }
}