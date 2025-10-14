package com.sevencourts.sandbox.app1loader;

import android.content.Context;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class APKSignatureChecker {

    private static final String TAG = APKSignatureChecker.class.getSimpleName();

    /**
     * Verifies that the APK file is signed with the trusted public key.
     * @param apkFile The APK file to verify.
     * @param trustedCertAssetName The filename of the trusted .cer file in the assets folder.
     * @return true if the signature is valid, false otherwise.
     */
    public static boolean isSignatureValid(Context ctx, File apkFile, String trustedCertAssetName) {
        try {
            // 1. Load the trusted public key from the .cer file in assets
            PublicKey trustedPublicKey;
            try (InputStream is = ctx.getAssets().open(trustedCertAssetName)) {
                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                X509Certificate trustedCert = (X509Certificate) cf.generateCertificate(is);
                trustedPublicKey = trustedCert.getPublicKey();
            }

            // 2. Get the signature from the APK file
            android.content.pm.PackageManager pm = ctx.getPackageManager();
            android.content.pm.PackageInfo pi = pm.getPackageArchiveInfo(apkFile.getAbsolutePath(), android.content.pm.PackageManager.GET_SIGNING_CERTIFICATES);
            if (pi == null) {
                Log.d(TAG, "PackageInfo is missing");
                return false;
            }
            if (pi.signingInfo == null) {
                Log.d(TAG, "PackageInfo contains no signing info");
                return false;
            }
            android.content.pm.Signature[] signatures = pi.signingInfo.getApkContentsSigners();
            if (signatures == null || signatures.length == 0) {
                Log.d(TAG, "No APK contents signers found");
                return false;
            }

            // 3. Extract the public key from the APK's signature
            android.content.pm.Signature apkSignature = signatures[0];
            byte[] apkCertBytes = apkSignature.toByteArray();
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            X509Certificate apkCert = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(apkCertBytes));
            PublicKey apkPublicKey = apkCert.getPublicKey();

            // 4. Compare the keys
            boolean keysAreEqual = trustedPublicKey.equals(apkPublicKey);
            Log.d(TAG, String.format("Public key %s", keysAreEqual ? "match OK" : "does NOT match"));
            return keysAreEqual;

        } catch (Exception e) {
            Log.e(TAG, "Could not verify signature", e);
            return false;
        }
    }
}
