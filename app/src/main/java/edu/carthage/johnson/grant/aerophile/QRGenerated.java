package edu.carthage.johnson.grant.aerophile;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Display;
import android.view.WindowManager;
import android.widget.ImageView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class QRGenerated extends ActionBarActivity {
    Project project;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrgenerated);
        HostProxy hostProxy = new HostProxy();
        hostProxy.execute();
        //Find screen size
        WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        int width = point.x;
        int height = point.y;
        int smallerDimension = width < height ? width : height;
        smallerDimension = smallerDimension * 3/4;
        String qrInputText = "";
        if(getIntent().hasExtra("toGen"))
        {
            qrInputText = getIntent().getStringExtra("toGen");
        }

        if(getIntent().hasExtra("Project"))
        {
            project = getIntent().getParcelableExtra("Project");
        }

        //Encode with a QR Code image
        QREncoder qrCodeEncoder = new QREncoder(qrInputText,
                null,
                Contents.Type.TEXT,
                BarcodeFormat.QR_CODE.toString(),
                smallerDimension);
        try {
            Bitmap bitmap = qrCodeEncoder.encodeAsBitmap();
            ImageView myImage = (ImageView) findViewById(R.id.theCode);
            myImage.setImageBitmap(bitmap);

        } catch (WriterException e) {
            e.printStackTrace();
        }


    }

    private class HostProxy extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params)
        {
            try
            {
                ServerSocket hostSocket = new ServerSocket(55555);//Socket to accept message from the partner
                Socket mySocket = hostSocket.accept();//Blocking call
                ObjectInputStream objIS = new ObjectInputStream(mySocket.getInputStream());
                MessageToHost messageFromPartner = (MessageToHost) objIS.readObject();


                //Now we have the message from the partner, we should be able to send them a filestructure. Hopefully.
                if(messageFromPartner.isToConnect())//If the partner wants to connect (ie, be sent the file structure)
                {
                    Socket toPartner = new Socket(messageFromPartner.getPartnerIP(), 55555);

                    FileStructure fileStructure = new FileStructure(new File(project.getFilepath()));
                    ObjectOutputStream objOS = new ObjectOutputStream(toPartner.getOutputStream());
                    objOS.writeObject(fileStructure);

                }



            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            return null;
        }
    }

}
