package com.tadvice.learnmaps

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Status
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.*
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.tadvice.learnmaps.databinding.FragmentFirstBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

/**
 * A simple [Fragment] subclass.
 * Use the [FirstFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null;
    private val binding get() = _binding!!;
    private var placesClient:PlacesClient?=null;
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_first, container, false)
        _binding = FragmentFirstBinding.inflate(inflater,container,false);

        Log.d("FirstFragment", "onCreateView")


        // Initialize the SDK
        Places.initialize(requireContext(), getString(R.string.google_maps_key));

        // Create a new PlacesClient instance
        placesClient = Places.createClient(requireContext())

        // Initialize the AutocompleteSupportFragment.
        val autocompleteFragment =
            childFragmentManager.findFragmentById(R.id.autocomplete_fragment)
                    as AutocompleteSupportFragment

        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME))

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                // TODO: Get info about the selected place.
                Log.d("FirstFragment", "Place: ${place.name}, ${place.id}")
                getPlacePhotoByPlaceId(place.id!!);
            }

            override fun onError(status: Status) {
                // TODO: Handle the error.
                Log.d("FirstFragment", "An error occurred: $status")
            }
        })
        return binding.root;
    }

    fun getPlacePhotoByPlaceId(placeId : String){
        // Define a Place ID.
        //val placeId = "INSERT_PLACE_ID_HERE"
        Log.d("FirstFragment", "getPlacePhoto: placeId ${placeId}")
// Specify fields. Requests for photos must always have the PHOTO_METADATAS field.
        val placeFields: List<Place.Field> = listOf(
            Place.Field.ID,
            Place.Field.NAME,
            Place.Field.ADDRESS,
            Place.Field.PHOTO_METADATAS
        );

// Get a Place object (this example uses fetchPlace(), but you can also use findCurrentPlace())
        val placeRequest = FetchPlaceRequest.newInstance(placeId, placeFields)

        placesClient!!.fetchPlace(placeRequest)
            .addOnSuccessListener { response: FetchPlaceResponse ->
                val place = response.place
                Log.d("FirstFragment", "fetchPlace place ${place}");

                // Get the photo metadata.
                val metada = place.photoMetadatas
                if (metada == null || metada.isEmpty()) {
                    Log.d("FirstFragment", "No photo metadata.")
                    return@addOnSuccessListener
                }
                val photoMetadata = metada.first()
                Log.d("FirstFragment", "photoMetadata ${photoMetadata}");
                // Get the attribution text.
                val attributions = photoMetadata?.attributions
                Log.d("FirstFragment", "attributions ${attributions}");

                // Create a FetchPhotoRequest.
                val photoRequest = FetchPhotoRequest.builder(photoMetadata)
                    .setMaxWidth(500) // Optional.
                    .setMaxHeight(300) // Optional.
                    .build()
                placesClient!!.fetchPhoto(photoRequest)
                    .addOnSuccessListener { fetchPhotoResponse: FetchPhotoResponse ->
                        val bitmap = fetchPhotoResponse.bitmap
                        Log.d("FirstFragment", "bitmap ${bitmap}");

                        binding.imageView.setImageBitmap(bitmap);
                        //imageView.setImageBitmap(bitmap)
                    }.addOnFailureListener { exception: Exception ->
                        if (exception is ApiException) {
                            Log.d("FirstFragment", "Place not found: ${exception.message}" )
                            val statusCode = exception.statusCode
                            //TODO("Handle error with given status code.")
                            Log.d("FirstFragment", "exception statusCode ${statusCode}")

                        }
                    }
            }

    }
}