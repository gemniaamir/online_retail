package com.codecanyon.onlinestore.ui.profile;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;
import com.codecanyon.onlinestore.AddressList;
import com.codecanyon.onlinestore.FavouriteView;
import com.codecanyon.onlinestore.GlobalVariable;
import com.codecanyon.onlinestore.LoginPage;
import com.codecanyon.onlinestore.OrderHistory;
import com.codecanyon.onlinestore.ProfileDetail;
import com.codecanyon.onlinestore.R;
import com.codecanyon.onlinestore.databinding.FragmentProfileBinding;
import com.codecanyon.onlinestore.models.UserClass;
import com.sdsmdg.tastytoast.TastyToast;

import java.io.FileNotFoundException;
import java.io.InputStream;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {

    private static final int RESULT_LOAD_IMG = 100;
    private FragmentProfileBinding binding;
    AppCompatButton personalInfo, myOrders, myFavorites, myAddresses, logout;
    CircleImageView profileImage;
    TextView email, username;

    UserClass loggedInUser;
    SharedPreferences mPrefs;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        myFavorites = root.findViewById(R.id.myFavorites);
        personalInfo = root.findViewById(R.id.myInfo);
        myOrders = root.findViewById(R.id.myOrders);
        logout = root.findViewById(R.id.logout);
        profileImage = root.findViewById(R.id.profile_image);
        username = root.findViewById(R.id.username);
        email = root.findViewById(R.id.email);
        myAddresses = root.findViewById(R.id.myAddresses);

        mPrefs = getActivity().getSharedPreferences(GlobalVariable.sharedPrefName, MODE_PRIVATE);

        myOrders.setOnClickListener(view -> {
            startActivity(new Intent(getContext(), OrderHistory.class));
            getActivity().overridePendingTransition(R.anim.slide_in_right,R.anim.stay);
        });

        myAddresses.setOnClickListener(view -> {
            startActivity(new Intent(getContext(), AddressList.class));
            getActivity().overridePendingTransition(R.anim.slide_in_right,R.anim.stay);
        });

        personalInfo.setOnClickListener(view -> {
            startActivity(new Intent(getContext(), ProfileDetail.class));
            getActivity().overridePendingTransition(R.anim.slide_in_right,R.anim.stay);
        });

        myFavorites.setOnClickListener(view -> {
            startActivity(new Intent(getContext(), FavouriteView.class));
            getActivity().overridePendingTransition(R.anim.slide_in_right,R.anim.stay);
        });

        logout.setOnClickListener(view -> {
            AuthUI.getInstance()
                    .signOut(getContext())
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        public void onComplete(@NonNull Task<Void> task) {
                            // user is now signed out
                            startActivity(new Intent(getContext(), LoginPage.class));
                            getActivity().finish();
                        }
                    });
        });

        profileImage.setOnClickListener(view -> getUserImageFromGallery());

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null){
            startActivity(new Intent(getContext(), LoginPage.class));
            getActivity().overridePendingTransition(R.anim.slide_in_right,R.anim.stay);
        }else {
            loadPreferencesAndViews();
        }
    }

    private void getUserImageFromGallery() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, RESULT_LOAD_IMG);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            try {
                final Uri imageUri = data.getData();
                final InputStream imageStream = getActivity().getContentResolver().openInputStream(imageUri);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                profileImage.setImageBitmap(selectedImage);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                TastyToast.makeText(getContext(), "Something went wrong", TastyToast.LENGTH_LONG, TastyToast.ERROR);
            }

        }else {
            TastyToast.makeText(getContext(), "No Image picked", TastyToast.LENGTH_LONG, TastyToast.ERROR);
        }
    }

    private void loadPreferencesAndViews(){
        Gson gson = new Gson();
        String userJson = mPrefs.getString("User", "");

        loggedInUser = gson.fromJson(userJson, UserClass.class);

        username.setText(loggedInUser.getFullName());
        email.setText(loggedInUser.getEmail());
    }

}