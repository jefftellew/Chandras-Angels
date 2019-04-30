package edu.ucsb.munchease.activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import edu.ucsb.munchease.R;
import edu.ucsb.munchease.data.Party;
import edu.ucsb.munchease.data.Restaurant;
import edu.ucsb.munchease.view.RestaurantAdapter;

public class PartyHomeActivity extends AppCompatActivity {

    //The party
    private Party party;

    //Visual components of the app

    private RecyclerView recyclerView_restaurantList;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;

    private FirebaseFirestore db;
    private DocumentReference docRef;

    //------------------------------------------------------------------
    //MEMBER FUNCTIONS
    //------------------------------------------------------------------

    /**
     * Called on the creation of the activity
     * @param savedInstanceState The previous state of the activity?
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_party_home);

        party = new Party();
        //party.addRestaurant(new Restaurant("Local Restaurant 1", "5", 25, "$$", "1234 The Street")); //Test restaurant

        setUpFirebase();
        populateDatabase();
        setUpRestaurantList();
        retrievePartyFromDatabase();
    }

    /**
     * Sets up the RecyclerView to show the list of restaurants
     */
    private void setUpRestaurantList() {
        recyclerView_restaurantList = findViewById(R.id.recyclerView_restaurantList);
        recyclerView_restaurantList.setHasFixedSize(true);

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(this);
        recyclerView_restaurantList.setLayoutManager(layoutManager);

        //Specify an adapter
        mAdapter = new RestaurantAdapter(party.getRestaurants());
        recyclerView_restaurantList.setAdapter(mAdapter);
    }

    /**
     * Initializes the Firebase references
     */
    private void setUpFirebase() {
        db = FirebaseFirestore.getInstance();
        docRef = db.collection("parties").document("123456");
    }

    /**
     * Adds dummy data to the databse
     */
    private void populateDatabase() {
        Party party2 = new Party();
        party2.addRestaurant(new Restaurant("DB Restaurant 1", "5", 25, "$$", "1234 The Street"));
        party2.addRestaurant(new Restaurant("DB Restaurant 2", "3", 50, "$$$$", "5678 An Avenue"));

        // Add a new document with a generated ID
        db.collection("parties").document(party2.getPartyID()).set(party2);
    }

    /**
     * Gets the latest information from the database and updates the RecyclerView adapter to reflect any changes
     * Currently does not properly support updating on data changes, and will just add to the end of the list instead of updating items
     */
    private void retrievePartyFromDatabase() {
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d("---RETRIEVE---", "DocumentSnapshot data: " + document.getData());

                        Party theParty = document.toObject(Party.class);
                        int startPosition = party.getRestaurants().size();

                        for(Restaurant r : theParty.getRestaurants()) {
                            party.addRestaurant(r);
                        }

                        mAdapter.notifyItemRangeInserted(startPosition, party.getRestaurants().size());
                    } else {
                        Log.d("---RETRIEVE---", "No such document");
                    }
                } else {
                    Log.d("---RETRIEVE---", "get failed with ", task.getException());
                }
            }
        });
    }
}
