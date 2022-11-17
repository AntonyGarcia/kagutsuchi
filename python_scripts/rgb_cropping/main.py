import os
from PIL import Image

index = 1
for pictures in os.scandir("../../images/rotated_images"):
    original_image = Image.open(pictures.path)
    left_image=original_image.crop((0,0,1920,1440))
    right_image = original_image.crop((1920, 0, 3840, 1440))
    left_image.save("../../images/split_images/left_img_" + str(index) + ".jpg")
    right_image.save("../../images/split_images/right_img_" + str(index) + ".jpg")
    index = index+1




