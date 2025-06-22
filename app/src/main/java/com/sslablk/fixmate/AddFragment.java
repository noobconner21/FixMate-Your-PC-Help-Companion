package com.sslablk.fixmate;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.sslablk.fixmate.ImagePreviewAdapter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class AddFragment extends Fragment {

    private static final String API_KEY = "X_API_KEY_HERE";
    private static final String TAG = "AddFragment";
    private static final int MAX_IMAGES = 3;

    private TextInputEditText titleEditText, descriptionEditText;
    private MaterialButton addPhotosButton;
    private RecyclerView imagePreviewRecyclerView;
    private ImagePreviewAdapter imagePreviewAdapter;
    private final List<Uri> selectedImageUris = new ArrayList<>();
    private ActivityResultLauncher<Intent> pickMultipleImagesLauncher;

    private LoadingDialog loadingDialog;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        pickMultipleImagesLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                Intent data = result.getData();
                if (data.getClipData() != null) {
                    int clipDataCount = data.getClipData().getItemCount();
                    int availableSlots = MAX_IMAGES - selectedImageUris.size();
                    int imagesToPick = Math.min(clipDataCount, availableSlots);

                    for (int i = 0; i < imagesToPick; i++) {
                        if (selectedImageUris.size() < MAX_IMAGES) {
                            selectedImageUris.add(data.getClipData().getItemAt(i).getUri());
                        } else {
                            break;
                        }
                    }
                } else if (data.getData() != null) {
                    if (selectedImageUris.size() < MAX_IMAGES) {
                        selectedImageUris.add(data.getData());
                    }
                }
                updateImagePreview();
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        titleEditText = view.findViewById(R.id.post_title_edittext);
        descriptionEditText = view.findViewById(R.id.post_description_edittext);
        addPhotosButton = view.findViewById(R.id.add_photos_button);
        imagePreviewRecyclerView = view.findViewById(R.id.image_preview_recyclerview);
        MaterialButton postButton = view.findViewById(R.id.post_button);

        imagePreviewRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        imagePreviewAdapter = new ImagePreviewAdapter(requireContext(), selectedImageUris);
        imagePreviewRecyclerView.setAdapter(imagePreviewAdapter);

        loadingDialog = new LoadingDialog(requireContext());

        addPhotosButton.setOnClickListener(v -> openMediaPicker());
        postButton.setOnClickListener(v -> createPost());
    }

    private void openMediaPicker() {
        if (selectedImageUris.size() < MAX_IMAGES) {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            pickMultipleImagesLauncher.launch(intent);
        } else {
            Toast.makeText(getContext(), "You can select a maximum of " + MAX_IMAGES + " images.", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateImagePreview() {
        imagePreviewAdapter.notifyDataSetChanged();
        imagePreviewRecyclerView.setVisibility(selectedImageUris.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void showLoadingDialog() {
        if (loadingDialog != null && !loadingDialog.isShowing()) {
            loadingDialog.show();
        }
    }

    private void hideLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
            loadingDialog = null;
        }
    }

    private void createPost() {
        String title = titleEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(description)) {
            Toast.makeText(getContext(), "Title and description are required.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedImageUris.isEmpty()) {
            Toast.makeText(getContext(), "Please add at least one image.", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "You must be logged in to post.", Toast.LENGTH_SHORT).show();
            return;
        }
        String userId = currentUser.getUid();

        List<MultipartBody.Part> imageParts = new ArrayList<>();
        for (Uri uri : selectedImageUris) {
            try {
                imageParts.add(prepareFilePart("images", uri));
            } catch (Exception e) {
                Toast.makeText(getContext(), "Error preparing an image.", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        RequestBody userIdPart = RequestBody.create(userId, MediaType.parse("text/plain"));
        RequestBody titlePart = RequestBody.create(title, MediaType.parse("text/plain"));
        RequestBody descriptionPart = RequestBody.create(description, MediaType.parse("text/plain"));

        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        showLoadingDialog();

        Call<CreatePostResponse> call = apiService.createPost(API_KEY, userIdPart, titlePart, descriptionPart, imageParts);

        call.enqueue(new Callback<CreatePostResponse>() {
            @Override
            public void onResponse(@NonNull Call<CreatePostResponse> call, @NonNull Response<CreatePostResponse> response) {
                hideLoadingDialog();

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(getContext(), "Post created successfully!", Toast.LENGTH_LONG).show();

                    titleEditText.setText("");
                    descriptionEditText.setText("");
                    selectedImageUris.clear();
                    updateImagePreview();

                    if (getActivity() != null) {
                        BottomNavigationView bottomNavigationView = getActivity().findViewById(R.id.bottom_navigation); // Replace with your BottomNavigationView ID
                        if (bottomNavigationView != null) {
                            bottomNavigationView.setSelectedItemId(R.id.nav_post);
                        }
                    }

                } else {
                    Toast.makeText(getContext(), "Failed to create post.", Toast.LENGTH_LONG).show();
                    if (response.errorBody() != null) {
                        try {
                        } catch (Exception e) {
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<CreatePostResponse> call, @NonNull Throwable t) {
                hideLoadingDialog();
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @NonNull
    private MultipartBody.Part prepareFilePart(String partName, Uri fileUri) throws Exception {
        File file = new File(requireContext().getCacheDir(), "temp_image_" + System.currentTimeMillis() + ".jpg");
        try (InputStream inputStream = requireContext().getContentResolver().openInputStream(fileUri);
             OutputStream outputStream = new FileOutputStream(file)) {

            if (inputStream == null) {
                throw new Exception("Input stream is null for URI: " + fileUri);
            }

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.flush();
        } catch (Exception e) {
            throw e;
        }

        RequestBody requestFile = RequestBody.create(file,
                MediaType.parse(Objects.requireNonNull(requireContext().getContentResolver().getType(fileUri)))
        );

        return MultipartBody.Part.createFormData(partName, file.getName(), requestFile);
    }
}