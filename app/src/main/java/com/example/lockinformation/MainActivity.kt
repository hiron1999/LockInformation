package com.example.lockinformation

import android.app.KeyguardManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.security.keystore.KeyProperties
import android.text.Editable
import android.widget.Button
import android.widget.Toast
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKeys
import kotlinx.android.synthetic.main.activity_main.*
import java.io.*
import java.util.concurrent.Executor

class MainActivity : AppCompatActivity() {
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private val file by lazy { File(filesDir, "myfile") }
    private val encriptedfile by lazy {
        EncryptedFile.Builder(
            file,
            applicationContext,
            masterkey,
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
        ).build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        Create.setOnClickListener {
            writefile(edit.text.toString().toByteArray(Charsets.UTF_8))
            edit.setText("")
        }
        fetch.setOnClickListener {
            authenticate_user()

        }


    }

    private fun writefile(byteArray: ByteArray) {
        if(file.exists()){
            file.delete()
        }
        try {
            encriptedfile.openFileOutput().apply {
                write(byteArray)
                flush()
                close()
            }
            Toast.makeText(applicationContext, "File saved", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(applicationContext, "${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun readfile(fileinput: ()->FileInputStream): java.lang.StringBuilder {
        val stringBuilder = StringBuilder()
            var inputStream: FileInputStream? = null
        try {
            inputStream=fileinput()
            val reader = BufferedReader(InputStreamReader(inputStream))

            reader.forEachLine { line -> stringBuilder.appendln(line) }

        } catch (e: Exception) {
            Toast.makeText(applicationContext, "${e.message}", Toast.LENGTH_SHORT).show()
        }finally {
            inputStream?.close()
        }
        return stringBuilder
    }
    private fun authenticate_user(){
        executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int,
                                                   errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(applicationContext,
                        "Authentication error: $errString", Toast.LENGTH_SHORT)
                        .show()
                }

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    Toast.makeText(applicationContext,
                        "Authentication succeeded!", Toast.LENGTH_SHORT)
                        .show()
                    edit.setText(readfile{encriptedfile.openFileInput()}.toString())
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(applicationContext, "Authentication failed",
                        Toast.LENGTH_SHORT)
                        .show()
                    edit.setText(readfile{file.inputStream()}.toString())
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock ?")
            .setSubtitle("Would you like to unlock the content ?")
            .setNegativeButtonText("use account password")

            .build()
        biometricPrompt.authenticate(promptInfo)
    }

    companion object{
        val masterkey=MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

    }

}