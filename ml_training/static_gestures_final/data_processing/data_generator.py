from random import randint
import os

def bent():
    return randint(90, 180)


def straight():
    return randint(0, 45)


def generate_readings():
    # print("THUMB, INDEX, MIDDLE, RING, LITTLE, LABEL")
    # for i in range(0, 500):
    #     thumb = bent()
    #     index = straight()
    #     middle = bent()
    #     ring = bent()
    #     pinky = bent()
    #     print(f'{thumb},{index},{middle},{ring},{pinky},1')
    #
    # for i in range(0, 500):
    #     thumb = bent()
    #     index = straight()
    #     middle = straight()
    #     ring = bent()
    #     pinky = bent()
    #     print(f'{thumb},{index},{middle},{ring},{pinky},2')
    #
    # for i in range(0, 500):
    #     thumb = straight()
    #     index = straight()
    #     middle = straight()
    #     ring = bent()
    #     pinky = bent()
    #     print(f'{thumb},{index},{middle},{ring},{pinky},3')
    #
    # for i in range(0, 500):
    #     thumb = bent()
    #     index = straight()
    #     middle = straight()
    #     ring = straight()
    #     pinky = straight()
    #     print(f'{thumb},{index},{middle},{ring},{pinky},4')
    #
    # for i in range(0, 500):
    #     thumb = straight()
    #     index = straight()
    #     middle = straight()
    #     ring = straight()
    #     pinky = straight()
    #     print(f'{thumb},{index},{middle},{ring},{pinky},5')
    #
    # for i in range(0, 500):
    #     thumb = bent()
    #     index = straight()
    #     middle = straight()
    #     ring = straight()
    #     pinky = bent()
    #     print(f'{thumb},{index},{middle},{ring},{pinky},6')
    #
    # for i in range(0, 500):
    #     thumb = bent()
    #     index = straight()
    #     middle = straight()
    #     ring = bent()
    #     pinky = straight()
    #     print(f'{thumb},{index},{middle},{ring},{pinky},7')
    #
    # for i in range(0, 500):
    #     thumb = bent()
    #     index = straight()
    #     middle = bent()
    #     ring = straight()
    #     pinky = straight()
    #     print(f'{thumb},{index},{middle},{ring},{pinky},8')
    #
    # for i in range(0, 500):
    #     thumb = bent()
    #     index = bent()
    #     middle = straight()
    #     ring = straight()
    #     pinky = straight()
    #     print(f'{thumb},{index},{middle},{ring},{pinky},9')
    #
    # for i in range(0, 500):
    #     thumb = straight()
    #     index = bent()
    #     middle = bent()
    #     ring = bent()
    #     pinky = bent()
    #     print(f'{thumb},{index},{middle},{ring},{pinky},10')

    for i in range(0, 500):
        thumb = straight()
        index = bent()
        middle = bent()
        ring = bent()
        pinky = straight()
        print(f'{thumb},{index},{middle},{ring},{pinky},Y')


generate_readings()
# print(os.getcwd())
