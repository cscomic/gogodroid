IMAGES=gogo6_icon.svg gogo6_offline.svg
RES=hdpi ldpi mdpi xhdpi

TARGET_DIRS=$(foreach res, $(RES), res/drawable-$(res))
TARGETS=$(foreach img, $(IMAGES), $(foreach dir, $(TARGET_DIRS), $(dir)/$(addsuffix .png, $(basename $(img)))))

all: $(TARGETS)

res/drawable-ldpi/%.png: %.svg
	convert -background none $< -resize 32x32 $@

res/drawable-mdpi/%.png: %.svg
	convert -background none $< -resize 48x48 $@

res/drawable-hdpi/%.png: %.svg
	convert -background none $< -resize 72x72 $@

res/drawable-xhdpi/%.png: %.svg
	convert -background none $< -resize 96x96 $@

clean:
	rm -f $(TARGETS)
