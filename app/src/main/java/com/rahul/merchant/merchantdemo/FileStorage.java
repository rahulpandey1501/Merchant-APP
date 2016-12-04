package com.rahul.merchant.merchantdemo;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.InputStream;

/**
 * Created by root on 12/4/16.
 */

abstract class FileStorage {

    private StorageReference storageReference;
    private StorageMetadata metadata;
    private String fileName;
    protected abstract void onProgress(int progress);
    protected abstract void onSuccess(Uri uri);
    protected abstract void onFailure();

    FileStorage(Context mContext, String fileName) {
        this.fileName = fileName;
        createFirebaseStorageInstance();
    }

    private void createFirebaseStorageInstance() {
        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        StorageReference reference = firebaseStorage.getReferenceFromUrl("gs://study-merchant.appspot.com");
        storageReference = reference.child("images/" + fileName);
        metadata = new StorageMetadata.Builder()
                .setContentType("image/jpeg")
                .build();
    }

    void startUpload(Uri uri, String mimeType) {
        metadata = new StorageMetadata.Builder()
                .setContentType(mimeType)
                .build();
        startUpload(uri);
    }

    void startUpload(Uri uri) {
        UploadTask uploadTask = storageReference.putFile(uri, metadata);
        uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                int progress = (int) ((100 * taskSnapshot.getBytesTransferred())/taskSnapshot.getTotalByteCount());
                Utility.log(progress + "% uploaded");
                FileStorage.this.onProgress(progress);
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                FileStorage.this.onSuccess(downloadUrl);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                FileStorage.this.onFailure();
            }
        });
    }
}
