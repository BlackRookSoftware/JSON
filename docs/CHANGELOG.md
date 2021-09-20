JSON (C) Black Rook Software 2019-2021
======================================
by Matt Tropiano et al. (see AUTHORS.txt)


Changed in 1.1.3
----------------

- `Fixed` Reduced memory allocations in JSONWriter.
- `Changed` A slight tweak to member gathering.


Changed in 1.1.2
----------------

- `Changed` JSONObject.create(T) will return the passed in object itself if that object is an instance of JSONObject.
  This was done so that JSONObjects can be generic members of deserialized/serialized data. Discussion will need to happen if this
  method should deep-copy the JSONObject instead. 


Changed in 1.1.1
----------------

- `Fixed` Array types are fixed.


Changed in 1.1.0
----------------

- `Added` Generic collection hints for erased types (JSONCollectionType, JSONMapType).


Changed in 1.0.0
----------------

- Base release.
