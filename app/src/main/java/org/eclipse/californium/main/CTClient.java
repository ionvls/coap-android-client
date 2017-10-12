package org.eclipse.californium.main;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Base64;
import com.android.volley.RequestQueue;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class CTClient
{
    private boolean debug;

    public CTClient(boolean debug)
    {
        this.debug = debug;
    }

    public String coapRequest(String coapURI, RequestQueue mRequestQueue)
    {
        String coapResponse ="";
        try
        {
            URI uri = new URI(coapURI);
            CoapClient client = new CoapClient(uri);
            CoapResponse response = client.get();
            System.out.println(coapURI);
            if (response.getCode() == ResponseCode.UNAUTHORIZED)
            {
                String responeText = response.getResponseText();
                System.out.println("CoAP repsonse: " + responeText);
                JSONObject jsonObject = new JSONObject(responeText);
                if(debug)
                {
                    System.out.println("Resource protected, authenticating to: " + jsonObject.getString("policy_uri"));
                }
                String token64 = jsonObject.getString("token");
                String keyIV64 = getkeyIV64(jsonObject.getString("policy_uri"), "RIOT1", "username","password",token64, mRequestQueue);
                if(debug)
                {
                    System.out.println("Authorized, requesting resource again");
                }
                uri = new URI(coapURI + "?token="+token64);
                System.out.println(uri);
                client = new CoapClient(uri);
                response = client.get();
                byte[] ciphertext = response.getPayload();
                byte[] keyIV = Base64.decode(keyIV64,Base64.NO_WRAP);
                byte[] key = Arrays.copyOfRange(keyIV, 0, 16);
                byte[] IV = Arrays.copyOfRange(keyIV, 16, 32);
                IvParameterSpec iv = new IvParameterSpec(IV);
                SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
                Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
                cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
                byte[] cleartext = cipher.doFinal(ciphertext);
                coapResponse = new String(cleartext);


            }
        }catch(Exception e)
        {
            System.out.println("Exception " + e.toString());
            e.printStackTrace();
        }
        return coapResponse;
    }
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private String getkeyIV64(String policyURI, String thingID, String username, String password, String token, RequestQueue mRequestQueue)
    {
        String result = "";
        String requestBody = "{";
        requestBody+= "\"thing_id\":\"" + thingID + "\"";
        requestBody+= ",\"username\":\"" + username + "\"";
        requestBody+= ",\"password\":\"" + password + "\"";
        requestBody+= ",\"token\":\"" + token + "\"";
        requestBody+= "}";
        try
        {
            URI policyurl = new URI(policyURI);
            HttpURLConnection httpConnection = (HttpURLConnection) policyurl.toURL().openConnection();
            httpConnection.setRequestMethod("POST");
            httpConnection.setDoOutput(true);
            httpConnection.setDoInput(true);

            OutputStream outputStream = new BufferedOutputStream(httpConnection.getOutputStream());
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "utf-8"));
            writer.write(requestBody);
            writer.flush();
            writer.close();
            httpConnection.getResponseCode();

            result = new BufferedReader(new InputStreamReader(httpConnection.getInputStream())).readLine();

        }catch (Exception e)
        {
            System.out.println("Exception in authenticating: " + e.toString());
            e.printStackTrace();

        }
        return result;

    }
}
