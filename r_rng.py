""" A simple module that uses the RPY2 module to overwrite the main methods 
of the RANDOM module. This facilitates the use of the same random number 
generator in models written in R and Python for docking proposes. Nota Bene: 
Some of the native sampling functions in R are very succinct and request 
numbers less frequently than a custom Python function would so you might need
to rewrite the R script to make sure it queries the random number generator 
the same number of times."""

import rpy2.robjects as robjects

def seed(value):
    robjects.r("function(x) set.seed(x)")(value)

def sample(pop, count):
    name_dict = {}
    for i in range(len(pop)):
        name_dict[i] = pop[i]
    vals = robjects.r("function(x,y) sample(x,y)")(name_dict.keys(),count)
    spots = []
    for k in vals:
        spots.append(list(k)[0])
    final = []
    for j in spots:
            final.append(name_dict[j])
    
    return final

def randint(low, high):
    return sample(range(low, high+1),1)[0]

def random():
    return round(list(robjects.r("runif(1)"))[0],7)

def shuffle(thing):
    name_dict = {}
    for i in range(len(thing)):
        name_dict[i] = thing[i]
    vals = robjects.r("function(x,y) sample(x,y)")(name_dict.keys())
    
    spots = []
    for k in vals:
        spots.append(list(k)[0])
    
    final = []
    for j in spots:
            final.append(name_dict[j])
    
    return final

def choice(thing):
    return sample(thing,1)[0]

