# MobileAndSocialSensing

### WheelChairApp



sematics: Elevator, Rough Road, Staircase


DB: Mongo/Firebase

Sensors: Accelerometer, Barometer, GPS


### Database structure

```JSON
{
    "_id": "ID",
    "position":{
        "latitude": "number",
        "longitude": "number",
    },
    "counter": "number",
    "timestamp": "YYYY-MM-DDTHH-MM-SSZ",
    "type": "elevator" | "rough rode" | "staircase"
}
```
