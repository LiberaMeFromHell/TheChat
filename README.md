# The Chat

One-room chat with text and image messages.

### Functionality

Main screen which holds ViewPager, which updates when a new message occurs. The chat has text and image messages.

## Structure 

The app consists of the Android client and backend in Firebase. There are two authentication supported: Google and via email.

### Used APIs

* Firebase DB
* Firebase storage
* Glide

## TODO

* Image button causes NullPointerException
* Replace ViewPager with RecyclerView
* Keep users authenticated
* Add notification when a new message occurs
* Implement MVVM or MVP pattern
* Add unit tests
