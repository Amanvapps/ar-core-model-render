package com.example.arappdemo;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.ar.core.Anchor;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Trackable;
import com.google.ar.core.TrackingState;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.animation.ModelAnimator;
import com.google.ar.sceneform.rendering.AnimationData;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;


import java.lang.ref.WeakReference;
import java.util.List;

public class MainActivity extends AppCompatActivity  {

    private ArFragment arFragment ;
    private PointerDrawable pointer = new PointerDrawable();
    private boolean isTracking ;
    private boolean isHitting ;
    private ModelLoader modelLoader;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        modelLoader = new ModelLoader(new WeakReference<>(this));


       arFragment = (ArFragment)getSupportFragmentManager().findFragmentById(R.id.sceneform_fragment);

       arFragment.getArSceneView().getScene().addOnUpdateListener(frameTime ->
               {arFragment.onUpdate(frameTime);
               onUpdate();
               }
               );


        initializeGallery();


    }

    private void onUpdate()
    {
        boolean trackingChanged = updateTracking();
        View contentView = findViewById(android.R.id.content);
        if(trackingChanged)
        {
            if(isTracking)
            {
                contentView.getOverlay().add(pointer);
            }
            else
            {
                contentView.getOverlay().remove(pointer);
            }
            contentView.invalidate();
        }

        if(isTracking)
        {
            boolean hitTestChanged = updateHitTest();
            if(hitTestChanged)
            {
                pointer.setEnabled(isHitting);
                contentView.invalidate();
            }
        }





    }

    private boolean updateHitTest()
    {
        Frame frame = arFragment.getArSceneView().getArFrame() ;
        android.graphics.Point pt = getScreenCenter();
        List<HitResult> hits ;
        boolean wasHitting = isHitting ;
        isHitting = false ;
        if(frame!=null)
        {
            hits = frame.hitTest(pt.x , pt.y);
            for(HitResult hit : hits)
            {
                Trackable trackable = hit.getTrackable();
                if(trackable instanceof Plane &&
                        ((Plane)trackable).isPoseInPolygon((hit.getHitPose())))
                {
                    isHitting = true ;
                    break ;
                }
            }
        }

        return wasHitting!=isHitting ;


    }

    private android.graphics.Point getScreenCenter()
    {
        View vw = findViewById(android.R.id.content);
        return new android.graphics.Point(vw.getWidth()/2 , vw.getHeight()/2);
    }

    private boolean updateTracking()
    {
        Frame frame = arFragment.getArSceneView().getArFrame();
        boolean wasTracking = isTracking;
        isTracking = frame!=null && frame.getCamera().getTrackingState() == TrackingState.TRACKING ;
        return  isTracking!= wasTracking ;
    }

    private void initializeGallery() {
        LinearLayout gallery = findViewById(R.id.gallery_layout);

        ImageView andy = new ImageView(this);
        andy.setImageResource(R.drawable.droid_thumb);
        andy.setContentDescription("andy");
        andy.setOnClickListener(view ->{addObject(Uri.parse("andy.sfb"));});
        gallery.addView(andy);

        ImageView tiger = new ImageView(this);
       tiger.setImageResource(R.drawable.ic_launcher_background);
        tiger.setContentDescription("Tiger");
        tiger.setOnClickListener(view ->{addObject(Uri.parse("tiger.sfb"));});
        gallery.addView(tiger);

        ImageView cabin = new ImageView(this);
        cabin.setImageResource(R.drawable.cabin_thumb);
        cabin.setContentDescription("cabin");
        cabin.setOnClickListener(view ->{addObject(Uri.parse("Cabin.sfb"));});
        gallery.addView(cabin);

        ImageView house = new ImageView(this);
        house.setImageResource(R.drawable.house_thumb);
        house.setContentDescription("house");
        house.setOnClickListener(view ->{addObject(Uri.parse("House.sfb"));});
        gallery.addView(house);

        ImageView igloo = new ImageView(this);
        igloo.setImageResource(R.drawable.igloo_thumb);
        igloo.setContentDescription("igloo");
        igloo.setOnClickListener(view ->{addObject(Uri.parse("igloo.sfb"));});
        gallery.addView(igloo);
    }

    public class ModelLoader {
        private final WeakReference<MainActivity> owner;
        private static final String TAG = "ModelLoader";

        ModelLoader(WeakReference<MainActivity> owner) {
            this.owner = owner;
        }

        void loadModel(Anchor anchor, Uri uri) {
            if (owner.get() == null) {
                Log.d(TAG, "Activity is null.  Cannot load model.");
                return;
            }
            ModelRenderable.builder()
                    .setSource(owner.get(), uri)
                    .build()
                    .handle((renderable, throwable) -> {
                        MainActivity activity = owner.get();
                        if (activity == null) {
                            return null;
                        } else if (throwable != null) {
                            activity.onException(throwable);
                        } else {
                            activity.addNodeToScene(anchor, renderable);
                        }
                        return null;
                    });

            return;
        }
    }






    private void addObject(Uri model) {
        Frame frame = arFragment.getArSceneView().getArFrame();
        android.graphics.Point pt = getScreenCenter();
        List<HitResult> hits;
        if (frame != null) {
            hits = frame.hitTest(pt.x, pt.y);
            for (HitResult hit : hits) {
                Trackable trackable = hit.getTrackable();
                if (trackable instanceof Plane &&
                        ((Plane) trackable).isPoseInPolygon(hit.getHitPose())) {
                    modelLoader.loadModel(hit.createAnchor(), model);
                    break;

                }
            }
        }
    }

    public void onException(Throwable throwable){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(throwable.getMessage())
                .setTitle("Codelab error!");
        AlertDialog dialog = builder.create();
        dialog.show();
        return;
    }

     public void addNodeToScene(Anchor anchor, ModelRenderable renderable) {
        AnchorNode anchorNode = new AnchorNode(anchor);
        TransformableNode node = new TransformableNode(arFragment.getTransformationSystem());
        node.setRenderable(renderable);
        node.setParent(anchorNode);
        arFragment.getArSceneView().getScene().addChild(anchorNode);
        node.select();
         startAnimation(node, renderable);
        }


    AnimationData animationData ;
    public void startAnimation(TransformableNode node, ModelRenderable renderable){
        if(renderable==null || renderable.getAnimationDataCount() == 0) {
            return;
        }
        for(int i = 0;i < renderable.getAnimationDataCount();i++){
            animationData = renderable.getAnimationData(i);
        }
        ModelAnimator animator = new ModelAnimator(animationData, renderable);


        node.setOnTapListener(
                (hitTestResult, motionEvent) -> {
                    togglePauseAndResume(animator);
                });
    }


    public void togglePauseAndResume(ModelAnimator animator) {
        if (animator.isPaused()) {
            animator.resume();
        } else if (animator.isStarted()) {
            animator.pause();
        } else {
            animator.start();
        }
    }

}
