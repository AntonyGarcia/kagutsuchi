import os
from PIL import Image

index = 1
for it in os.scandir("../../images/original_images"):
    if it.is_dir():
        for pictures in os.scandir(it.path):
            original_image = Image.open(pictures.path)
            rotated_image = original_image.rotate(180)
            rotated_image.save("../../images/rotated_images/img_"+str(index)+".jpg")
            index = index+1


