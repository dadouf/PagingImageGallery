Structure / How to read this code:
- All implementations exist simultaneously, without needing to change branches. They're grouped into two packages: `recyclerview` and `viewpager`.
- Within a group, each version is a different package: `v1`, `v2`, ..., `vfinal`. Since each version contains its own full CarouselActivity, you can easily compare one implementation with another.