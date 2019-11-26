package me.kindeep.thisisacalender;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class ContactsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        final Data data = new Data(this);

        final RecyclerView contactsRecycler = findViewById(R.id.contacts_recycler);

        final RecyclerView.Adapter contactsAdapter = new RecyclerView.Adapter() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View v =LayoutInflater.from(parent.getContext() ).inflate(R.layout.contacts_list_item, parent, false);
                return new ContactHolder(v);
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                ContactHolder colder = (ContactHolder) holder;
                final Meeting currMeeting = data.getMeetingsWithContact().get(position);
                final Contact contact = Data.getContactById(ContactsActivity.this, currMeeting.getContactId());
                colder.contactName.setText(contact.getName());
                colder.contactNumber.setText(contact.getPhone());
                colder.parent.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        openContactInSystemViewer(contact.getContactId());
                        notifyDataSetChanged();
                    }
                });
                colder.deleteBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        data.removeContact(contact);
                        notifyDataSetChanged();
                    }
                });
            }

            @Override
            public int getItemCount() {
                return data.getMeetingsWithContact().size();
            }
        };

        contactsRecycler.setAdapter(contactsAdapter);
        contactsRecycler.setLayoutManager(new LinearLayoutManager(this));
    }

    public void openContactInSystemViewer(String contactId) {
        // Open contact using the default contacts viewer
        if (contactId != null) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, contactId);
                intent.setData(uri);
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "Something went wrong with opening the contact.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "No contact associated with event!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e("idk", "idk");
    }

    class ContactHolder extends RecyclerView.ViewHolder {
        View parent;
        TextView contactName;
        TextView contactNumber;
        ImageButton deleteBtn;

        public ContactHolder(@NonNull View itemView) {
            super(itemView);
            parent = itemView;
            contactName = itemView.findViewById(R.id.contact_name);
            contactNumber = itemView.findViewById(R.id.contact_number);
            deleteBtn = itemView.findViewById(R.id.delete_contact_btn);
        }
    }
}
